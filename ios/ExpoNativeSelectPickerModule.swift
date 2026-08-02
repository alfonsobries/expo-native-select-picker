import ExpoModulesCore
import UIKit

struct SelectItem: Record {
  @Field var value: String = ""
  @Field var label: String = ""
}

struct SelectPresentOptions: Record {
  @Field var title: String?
  @Field var options: [SelectItem] = []
  @Field var selectedValue: String?
  @Field var searchable: Bool = true
  @Field var searchPlaceholder: String?
  // A–Z sections with the side index. nil = automatic (on for long lists).
  @Field var grouped: Bool?
  // "auto" (default), "sheet" or "fullScreen".
  @Field var presentation: String = "auto"
}

public class ExpoNativeSelectPickerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoNativeSelectPicker")

    AsyncFunction("presentAsync") { (options: SelectPresentOptions, promise: Promise) in
      guard let presenter = ExpoNativeSelectPickerModule.topViewController() else {
        promise.resolve(nil)
        return
      }

      let controller = SelectListViewController(options: options) { value in
        promise.resolve(value)
      }
      let navigation = UINavigationController(rootViewController: controller)
      navigation.modalPresentationStyle = .pageSheet
      navigation.presentationController?.delegate = controller

      // A short list rests at the height of its own rows, with the grabber
      // that says it can be pulled away; a long one keeps the full sheet.
      if SelectListViewController.presentsAsSheet(options),
        let sheet = navigation.sheetPresentationController
      {
        let content = SelectListViewController.sheetHeight(for: options)
        sheet.detents = [
          .custom(identifier: .init("fitsContent")) { context in
            min(content, context.maximumDetentValue)
          }
        ]
        sheet.prefersGrabberVisible = true
      }

      presenter.present(navigation, animated: true)
    }
    .runOnQueue(.main)
  }

  static func topViewController() -> UIViewController? {
    let window = UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first { $0.isKeyWindow }
    var top = window?.rootViewController
    while let presented = top?.presentedViewController {
      top = presented
    }
    return top
  }
}

/// The system-settings style option list: a plain table with an optional native
/// search controller, A–Z sections with the side index, and a checkmark on the
/// selected row. Resolves with the picked value, or nil when dismissed.
final class SelectListViewController: UITableViewController, UISearchResultsUpdating,
  UIAdaptivePresentationControllerDelegate
{
  private static let autoGroupThreshold = 30
  private static let cellIdentifier = "option"

  private let options: SelectPresentOptions
  private let onFinish: (String?) -> Void
  private var finished = false

  private var sections: [(title: String?, items: [SelectItem])] = []
  private var searching = false

  private var grouped: Bool {
    options.grouped ?? (options.options.count >= Self.autoGroupThreshold)
  }

  /// Rows that still fit a sheet without it swallowing the screen.
  private static let sheetMaxRows = 8
  private static let rowHeight: CGFloat = 44
  private static let barHeight: CGFloat = 56

  /// `auto` reads the list: short, unsearchable and ungrouped rests at its
  /// own height. `sheet` and `fullScreen` say it outright.
  static func presentsAsSheet(_ options: SelectPresentOptions) -> Bool {
    switch options.presentation {
    case "sheet": return true
    case "fullScreen": return false
    default:
      let grouped = options.grouped ?? (options.options.count >= autoGroupThreshold)
      return !options.searchable && !grouped && options.options.count <= sheetMaxRows
    }
  }

  static func sheetHeight(for options: SelectPresentOptions) -> CGFloat {
    barHeight + CGFloat(options.options.count) * rowHeight + 16
  }

  init(options: SelectPresentOptions, onFinish: @escaping (String?) -> Void) {
    self.options = options
    self.onFinish = onFinish
    super.init(style: .plain)
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) {
    fatalError("init(coder:) is not supported")
  }

  override func viewDidLoad() {
    super.viewDidLoad()

    title = options.title

    // The grabber already says how a sheet goes away; a close button next
    // to it is one control too many.
    if !Self.presentsAsSheet(options) {
      navigationItem.leftBarButtonItem = UIBarButtonItem(
        barButtonSystemItem: .close,
        target: self,
        action: #selector(handleClose)
      )
    }

    if options.searchable {
      let search = UISearchController(searchResultsController: nil)
      search.searchResultsUpdater = self
      search.obscuresBackgroundDuringPresentation = false
      if let placeholder = options.searchPlaceholder {
        search.searchBar.placeholder = placeholder
      }
      navigationItem.searchController = search
      navigationItem.hidesSearchBarWhenScrolling = false
    }

    tableView.register(UITableViewCell.self, forCellReuseIdentifier: Self.cellIdentifier)
    rebuild(query: nil)
  }

  // MARK: - Data

  private func rebuild(query: String?) {
    let trimmed = query?.trimmingCharacters(in: .whitespaces) ?? ""
    searching = !trimmed.isEmpty

    let visible = searching
      ? options.options.filter {
        $0.label.range(of: trimmed, options: [.caseInsensitive, .diacriticInsensitive]) != nil
      }
      : options.options

    if grouped, !searching {
      var buckets: [String: [SelectItem]] = [:]
      for item in visible {
        buckets[Self.indexTitle(for: item.label), default: []].append(item)
      }
      sections = buckets.keys
        .sorted { ($0 == "#" ? "\u{10FFFF}" : $0) < ($1 == "#" ? "\u{10FFFF}" : $1) }
        .map { (title: $0, items: buckets[$0]!) }
    } else {
      sections = [(title: nil, items: visible)]
    }
    tableView.reloadData()
  }

  // First letter of the label ignoring diacritics and any leading symbols
  // (e.g. flag emoji in "🇲🇽 México"); non-letters land under "#".
  private static func indexTitle(for label: String) -> String {
    let folded = label.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: nil)
    if let letter = folded.unicodeScalars.first(where: { CharacterSet.letters.contains($0) }) {
      return String(letter).uppercased()
    }
    return "#"
  }

  // MARK: - Table

  override func numberOfSections(in tableView: UITableView) -> Int {
    sections.count
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    sections[section].items.count
  }

  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int)
    -> String?
  {
    sections[section].title
  }

  override func sectionIndexTitles(for tableView: UITableView) -> [String]? {
    guard grouped, !searching, sections.count > 1 else {
      return nil
    }
    return sections.compactMap { $0.title }
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath)
    -> UITableViewCell
  {
    let cell = tableView.dequeueReusableCell(
      withIdentifier: Self.cellIdentifier,
      for: indexPath
    )
    let item = sections[indexPath.section].items[indexPath.row]
    cell.textLabel?.text = item.label
    cell.accessoryType = item.value == options.selectedValue ? .checkmark : .none
    return cell
  }

  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    let item = sections[indexPath.section].items[indexPath.row]
    finish(item.value)
    dismiss(animated: true)
  }

  // MARK: - Dismissal

  @objc private func handleClose() {
    finish(nil)
    dismiss(animated: true)
  }

  // Swipe-down dismissal.
  func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
    finish(nil)
  }

  private func finish(_ value: String?) {
    if finished { return }
    finished = true
    onFinish(value)
  }

  // MARK: - Search

  func updateSearchResults(for searchController: UISearchController) {
    rebuild(query: searchController.searchBar.text)
  }
}
