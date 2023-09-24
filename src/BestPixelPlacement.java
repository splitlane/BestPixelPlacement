import java.util.Arrays;

public class BestPixelPlacement {
    // Helper for calculate
    public static Pixel getPixel(Board board, int x, int y) {
        if (x > -1 && x < board.sizex && y > -1 && y < board.sizey) {
            return board.board[y][x];
        } else {
            return new Pixel(x, y, PixelColor.NONE);
        }
    }


    /*
     * PixelPosition {x, y}:
     * y is number of rows from bottom: bottom row is 0.
     * x is number of columns from left: left column is 0.
     *
     * board[row][column]
     */
    public static Pixel calculate(Board board, PixelColor pixelColor) {
        /*
        Row: # columns
        0: 6
        1: 7
        2: 6
         */

        /*
         * Stategy: Brute force
         * 1: Find the places where we can place
         * 2: Check if place is "stable" (2 pixels left right same y)
         * 3: Automatically find best spot
         * 3a: If white pixel: Go for height
         * 3b: If color pixel: Go for mosaic
         */

        // Data structures that will be reused over and over
        Pixel pixel = new Pixel(0, 0, pixelColor);

        // 1: Find the places where we can place

        // For each row, topmost pixel of each column
        int[] columnsTopY = new int[board.sizex];
        for (int x = 0; x < board.sizex; x++) {
            if (board.board[board.sizey - 1][x].color != PixelColor.NONE) {
                // Edge case: Row is filled up all the way to the top
                columnsTopY[x] = -1;
            } else {
                if (x == board.sizex - 1) {
                    // Edge case: Pixel is leaning against the right wall
                    for (int y = board.sizey - 1; y > -1; y--) {
                        if (y == 0 || (board.board[y - 1][x].color != PixelColor.NONE)) {
                            columnsTopY[x] = y;
                            if (y % 2 == 0) {
                                columnsTopY[x]++;
                            }
                            break;
                        }
                    }
                } else {
                    for (int y = board.sizey - 1; y > -1; y--) {
                        if (y == 0 || (board.board[y - 1][x].color != PixelColor.NONE)) {
                            columnsTopY[x] = y;
                            break;
                        }
                    }
                }
            }
        }
//        System.out.println(Arrays.toString(columnsTopY));

        // Test: Mark pixels and print
//        for (int x = 0; x < columnsTopY.length; x++) {
//            if (columnsTopY[x] != -1) {
//                board.board[columnsTopY[x]][x].color = PixelColor.X_TEST;
//            }
//        }
//        System.out.println(board.toString());

        // 2: Check if place is "stable" (2 pixels left right same y)
        for (int x = 0; x < columnsTopY.length; x++) {
            int y = columnsTopY[x];
            if (y != -1) {
                int cx, cy;
                // The pixel is already stable on one pixel, which is the pixel that it is just above
                // Other pixel is left (if y % 2 == 1) and right (if y % 2 == 0)
                if (y % 2 == 0) {
                    // Check right bottom pixel
                    cx = x + 1;
                    cy = y - 1;
                } else {
                    // Check left bottom pixel
                    cx = x - 1;
                    cy = y - 1;
                }

                // Check
                if (cx == -1 || cx == board.sizex || cy == -1 || board.board[cy][cx].color != PixelColor.NONE) {
                    // Stable
                } else {
                    columnsTopY[x] = -1;
                }
            }
        }

        // Test: Mark pixels and print
//        for (int x = 0; x < columnsTopY.length; x++) {
//            if (columnsTopY[x] != -1) {
//                board.board[columnsTopY[x]][x].color = PixelColor.X_TEST;
//            }
//        }
//        System.out.println(board.toString());

        // Now, we have all the possible places where we can place.

        // 3: Automatically find best spot
        boolean useHeightOverride = false;
        if (pixelColor != PixelColor.WHITE) {
            // 3b: If color pixel: Go for mosaic
            /*
            Find mosaics
            If found:
            Go to biggest height for mosaic
            else:
            Go to biggest height (pass on to white only algorithm)
             */

            // Find mosaics (2 consecutive around pixel (same or different))
            boolean[] columnsIsMosaic = new boolean[columnsTopY.length];
            for (int x = 0; x < columnsTopY.length; x++) {
                int y = columnsTopY[x];
                if (y != -1) {
                    Pixel[] aroundPixels;
                    if (y % 2 == 0) {
                        aroundPixels = new Pixel[]{
                            getPixel(board, x, y + 1),
                            getPixel(board, x + 1, y + 1),
                            getPixel(board, x + 1, y),
                            getPixel(board, x + 1, y - 1),
                            getPixel(board, x, y - 1),
                            getPixel(board, x - 1, y),
                        };
                    } else {
                        aroundPixels = new Pixel[]{
                            getPixel(board, x - 1, y + 1),
                            getPixel(board, x, y + 1),
                            getPixel(board, x + 1, y),
                            getPixel(board, x, y - 1),
                            getPixel(board, x - 1, y - 1),
                            getPixel(board, x - 1, y),
                        };
                    }

                    boolean isMosaic = false;
                    PixelColor mosaicStreak = PixelColor.NONE;
                    for (int i = 0; i < aroundPixels.length; i++) {
                        PixelColor color = aroundPixels[i].color;
//                        System.out.println(color);
                        if (color == PixelColor.NONE || color == PixelColor.WHITE) {
                            // Reset streak
                            mosaicStreak = PixelColor.NONE;
                        } else if (color == pixelColor) {
                            // Check same color mosaic
                            if (mosaicStreak == pixelColor) {
                                isMosaic = true;
                                break;
                            } else {
                                mosaicStreak = pixelColor;
                            }
                        } else {
                            // Check diff color mosaic
                            if (mosaicStreak != pixelColor && mosaicStreak != PixelColor.NONE && mosaicStreak != color) {
                                isMosaic = true;
                                break;
                            } else {
                                mosaicStreak = color;
                            }
                        }
//                        System.out.println(isMosaic);

                        columnsIsMosaic[x] = isMosaic;

                    }
//                    System.out.println("");

                }
            }

            int cx, cy;
            cx = -1;
            cy = -1;
            for (int x = 0; x < columnsTopY.length; x++) {
                int y = columnsTopY[x];
                if (columnsIsMosaic[x]) {
                    if (y > cy) {
                        cx = x;
                        cy = y;
                    }
                }
            }
            if (cx != -1) {
                // If there are mosaics: then find highest one
                pixel.x = cx;
                pixel.y = cy;
            } else {
                // Else, find highest normal one (pass to white algorithm)
                useHeightOverride = true;
            }
        }

        if (pixelColor == PixelColor.WHITE || useHeightOverride) {
            // 3a: If white pixel: Go for height
            int cx, cy;
            cx = -1;
            cy = -2;
            for (int x = 0; x < columnsTopY.length; x++) {
                if (columnsTopY[x] > cy) {
                    cx = x;
                    cy = columnsTopY[x];
                }
            }

            if (cy == -1) {
                // Edge case: No stable place

                // Error: This cannot possibly happen.
            } else if (cy == -2) {
                // Edge case: No columns

                // Error: This cannot possibly happen.
            }

            pixel.x = cx;
            pixel.y = cy;
        }

        return pixel;
    }

    public static void main(String[] args) {
        Board board = new Board(7, 11);

        // set pixels
        int i = 0;
        PixelColor[] data = {
                PixelColor.YELLOW, PixelColor.YELLOW, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE
                , PixelColor.WHITE, PixelColor.YELLOW, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.PURPLE, PixelColor.WHITE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.PURPLE, PixelColor.PURPLE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.GREEN, PixelColor.GREEN, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.GREEN, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.WHITE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
                , PixelColor.WHITE, PixelColor.WHITE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
                , PixelColor.GREEN, PixelColor.YELLOW, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
                , PixelColor.PURPLE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE, PixelColor.NONE
        };
        for (int y = 0; y < board.sizey; y++) {
            for (int x = 0; x < board.sizex + ((y % 2 == 0) ? -1 : 0); x++) {
//                System.out.println(x + "," + y);
                board.board[y][x].color = data[i];
                i++;
            }
        }


        // Print board
        System.out.println(board);


        Pixel pixel = calculate(board, PixelColor.GREEN);
        System.out.println(pixel);
    }
}