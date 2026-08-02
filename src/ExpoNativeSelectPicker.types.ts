export type SelectOption = {
  value: string;
  /** Shown in the list; emoji are fine (e.g. "🇲🇽 México"). */
  label: string;
};

export type SelectPickerOptions = {
  /** Navigation-bar title of the sheet. */
  title?: string;
  options: SelectOption[];
  /** Value shown with a checkmark. */
  selectedValue?: string;
  /** Native search bar. Defaults to true. */
  searchable?: boolean;
  searchPlaceholder?: string;
  /**
   * A–Z sections with the side index. Defaults to automatic: on for lists of
   * 30+ options, off otherwise.
   */
  grouped?: boolean;
  /**
   * Colors, so the list can wear a design system instead of the platform
   * defaults. Any CSS-style hex string (`#111827`, `#fff`). Anything left
   * out falls back to the system color.
   *
   * iOS ignores these by design: there the list *is* the system sheet, and
   * repainting it would be the one thing that makes it look non-native.
   */
  colors?: SelectPickerColors;
};

export type SelectPickerColors = {
  /** Sheet background. */
  background?: string;
  /** Option labels and the title. */
  label?: string;
  /** Search placeholder, section headings, the search icon. */
  muted?: string;
  /** The check on the selected row and the A–Z index. */
  accent?: string;
};
