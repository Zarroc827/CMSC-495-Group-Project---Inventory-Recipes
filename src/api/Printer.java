package api;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class Printer implements Printable {
    final Component guiComponet;

    public Printer(Component guiComponet){
        this.guiComponet = guiComponet;
    }

    @Override
    public int print(Graphics g, PageFormat formatPage, int page_index)
            throws PrinterException {
        if (page_index > 0) {
            return Printable.NO_SUCH_PAGE;
        }
        Dimension dim = guiComponet.getSize();
        double pHeight = formatPage.getImageableHeight();
        double pWidth = formatPage.getImageableWidth();
        double guiHeight = dim.getHeight();
        double guiWidth = dim.getWidth();
        double xRatio = pWidth / guiWidth;
        double yRatio = pHeight / guiHeight;
        double pXStart = formatPage.getImageableX();
        double pYStart = formatPage.getImageableY();
        Graphics2D g1 = (Graphics2D) g;
        g1.translate(pXStart, pYStart);
        g1.scale(xRatio, yRatio);
        guiComponet.paint(g1);
        return Printable.PAGE_EXISTS;
    }
}
