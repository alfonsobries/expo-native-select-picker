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
};
