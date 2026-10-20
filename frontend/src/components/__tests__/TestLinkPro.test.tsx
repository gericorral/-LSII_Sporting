import { fireEvent, render, screen } from '@testing-library/react';
import TestLinkPro from '../TestLinkPro';

describe('Testlinkpro', () => {
  it('should call the mock', () => {
    const callMeMock = jest.fn();

    render(
      <TestLinkPro page="" onHover={() => callMeMock('Maybe')}>
        Hello
      </TestLinkPro>
    );
    fireEvent.mouseEnter(screen.getByText('Hello'));

    expect(callMeMock).toHaveBeenCalled();
  });

  it('should not call the mock', () => {
    const callMeMock = jest.fn();

    render(
      <TestLinkPro page="" onHover={() => callMeMock('Maybe')}>
        Hello
      </TestLinkPro>
    );

    expect(callMeMock).toHaveBeenCalledTimes(0);
  });

  it('supports hovering without a callback and resets its class', () => {
    render(<TestLinkPro page="test">Hello</TestLinkPro>);
    const link = screen.getByText('Hello');

    fireEvent.mouseEnter(link);
    expect(link).toHaveClass('hovered');
    fireEvent.mouseLeave(link);
    expect(link).toHaveClass('normal');
  });
});
