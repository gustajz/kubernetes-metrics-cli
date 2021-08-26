package com.github.gustajz.kubernetes.commons;

import java.util.*;

/** @author https://www.logicbig.com/how-to/code-snippets/jcode-java-cmd-command-line-table.html */
@SuppressWarnings("java:S106")
public class CommandLineTable {
    private static final String HORIZONTAL_SEP = "-";
    private String verticalSep;
    private String joinSep;
    private String[] headers;
    private List<Object[]> rows = new ArrayList<>();
    private boolean rightAlign;

    public CommandLineTable() {
        setShowVerticalLines(false);
    }

    public void setRightAlign(boolean rightAlign) {
        this.rightAlign = rightAlign;
    }

    public void setShowVerticalLines(boolean showVerticalLines) {
        verticalSep = showVerticalLines ? "|" : "";
        joinSep = showVerticalLines ? "+" : " ";
    }

    public void setHeaders(String... headers) {
        this.headers = headers;
    }

    public void addRow(Object... cells) {
        rows.add(cells);
    }

    public void print() {
        int[] maxWidths =
                headers != null ? Arrays.stream(headers).mapToInt(String::length).toArray() : null;

        for (Object[] cells : rows) {
            if (maxWidths == null) {
                maxWidths = new int[cells.length];
            }
            if (cells.length != maxWidths.length) {
                throw new IllegalArgumentException(
                        "Number of row-cells and headers should be consistent");
            }
            for (int i = 0; i < cells.length; i++) {
                maxWidths[i] = Math.max(maxWidths[i], Objects.toString(cells[i]).length());
            }
        }

        if (headers != null) {
            printLine(maxWidths);
            printRow(headers, maxWidths);
            printLine(maxWidths);
        }
        for (Object[] cells : rows) {
            printRow(cells, maxWidths);
        }
        if (headers != null) {
            printLine(maxWidths);
        }
    }

    private void printLine(int[] columnWidths) {
        for (int i = 0; i < columnWidths.length; i++) {
            String line =
                    String.join(
                            "",
                            Collections.nCopies(
                                    columnWidths[i] + verticalSep.length() + 1, HORIZONTAL_SEP));
            System.out.print(joinSep + line + (i == columnWidths.length - 1 ? joinSep : ""));
        }
        System.out.println();
    }

    @SuppressWarnings("java:S3457")
    private void printRow(Object[] cells, int[] maxWidths) {
        for (int i = 0; i < cells.length; i++) {
            Object s = cells[i];
            String verStrTemp = i == cells.length - 1 ? verticalSep : "";
            if (rightAlign) {
                System.out.printf("%s %" + maxWidths[i] + "s %s", verticalSep, s, verStrTemp);
            } else {
                System.out.printf("%s %-" + maxWidths[i] + "s %s", verticalSep, s, verStrTemp);
            }
        }
        System.out.println();
    }
}
