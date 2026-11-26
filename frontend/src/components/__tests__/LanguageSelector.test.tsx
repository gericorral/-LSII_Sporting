import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import LanguageSelector from '../LanguageSelector';

describe('LanguageSelector', () => {
  it('changes and persists the selected language', async () => {
    render(<LanguageSelector />);

    fireEvent.change(screen.getByRole('combobox', { name: 'Language' }), { target: { value: 'es' } });

    await waitFor(() => expect(screen.getByRole('combobox', { name: 'Idioma' })).toHaveValue('es'));
    expect(window.localStorage.getItem('protube-language')).toBe('es');
  });
});
