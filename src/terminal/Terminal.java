package terminal;
/**
 * A few {VT100 | ANSI | Xterm }-compatible sequences
 */

public class Terminal
{
    public static final char   ESC = '\033';
    public static final String CSI = ESC + "[";

    public enum Color { BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN };

    public static void CursorUp()       { System.out.print(CSI + 'A');  }
    public static void CursorDown()     { System.out.print(CSI + 'B');  }
    public static void CursorRight()    { System.out.print(CSI + 'C');  }
    public static void CursorLeft()     { System.out.print(CSI + 'D');  }
    public static void Home()           { System.out.print(CSI + 'H' ); }
    public static void ClearScreen()    { System.out.print(CSI + "2J"); }
    public static void Highlight()      { System.out.print(CSI + "1m"); }
    public static void Normal()         { System.out.print(CSI + "0m"); }
    public static void CursorOff()      { System.out.print(CSI + "?25l"); }
    public static void CursorOn()       { System.out.print(CSI + "?25h"); }
    public static void ClearEndOfLine() { System.out.print(CSI + 'K');  }
    public static void ClearLine()      { System.out.print(CSI + "2K"); }

    public static void GotoXY(int x, int y) { System.out.print(CSI + y + ';' + x + 'H'); }

    public static void SelectGraphicRendition(int code) { System.out.print(CSI + code + 'm'); }
    public static void Foreground(Color color) { SelectGraphicRendition(30 + color.ordinal()); }
    public static void Background(Color color) { SelectGraphicRendition(40 + color.ordinal()); }

    public static void BrightForeground(Color color) { SelectGraphicRendition(90 + color.ordinal()); }
    public static void BrightBackground(Color color) { SelectGraphicRendition(100 + color.ordinal()); }

}
