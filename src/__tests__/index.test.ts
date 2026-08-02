import { pickOption } from '../index';

const mockPresentAsync = jest.fn();

jest.mock('../ExpoNativeSelectPickerModule', () => ({
  __esModule: true,
  get default() {
    return { presentAsync: mockPresentAsync };
  },
}));

beforeEach(() => {
  mockPresentAsync.mockReset();
});

describe('pickOption', () => {
  it('resolves with the picked value', async () => {
    mockPresentAsync.mockResolvedValue('mx');

    await expect(pickOption({ options: [{ value: 'mx', label: '🇲🇽 México' }] })).resolves.toBe(
      'mx'
    );
  });

  it('resolves null when dismissed', async () => {
    mockPresentAsync.mockResolvedValue(null);

    await expect(pickOption({ options: [] })).resolves.toBeNull();
  });

  it('forwards the full options to the native module', async () => {
    mockPresentAsync.mockResolvedValue(null);
    const options = {
      title: 'Country',
      options: [{ value: 'mx', label: 'México' }],
      selectedValue: 'mx',
      searchable: true,
      searchPlaceholder: 'Search country',
      grouped: true,
    };

    await pickOption(options);

    expect(mockPresentAsync).toHaveBeenCalledWith(options);
  });
});
