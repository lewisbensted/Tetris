package tetris;

import java.util.*;

public class Tetris {
    static Integer[][] O = {{4, 14, 15, 5}};
    static Integer[][] I = {{4, 14, 24, 34}, {3, 4, 5, 6}};
    static Integer[][] S = {{5, 4, 14, 13}, {4, 14, 15, 25}};
    static Integer[][] Z = {{4, 5, 15, 16}, {5, 15, 14, 24}};
    static Integer[][] L = {{4, 14, 24, 25}, {5, 15, 14, 13}, {4, 5, 15, 25}, {6, 5, 4, 14}};
    static Integer[][] J = {{5, 15, 25, 24}, {15, 5, 4, 3}, {5, 4, 14, 24}, {4, 14, 15, 16}};
    static Integer[][] T = {{4, 14, 24, 15}, {4, 13, 14, 15}, {5, 15, 25, 14}, {4, 5, 6, 15}};

    static Map<String, Integer[][]> rotations = new HashMap<>();

    static {
        rotations.put("O", O);
        rotations.put("I", I);
        rotations.put("S", S);
        rotations.put("Z", Z);
        rotations.put("L", L);
        rotations.put("J", J);
        rotations.put("T", T);
    }

    static int width = 10;
    static int height;

    static Integer[][] activePiece = null;
    static int horizontalOffset = 0;
    static int verticalOffset = 0;

    static String[] board;

    static Scanner sc = new Scanner(System.in);

    static int variant = 0;


    public static void main(String[] args) {
        while (height == 0) {
            try {
                height = sc.nextInt();
                board = new String[height * width];
                Arrays.fill(board, "-");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
                height = 0;
                sc.nextLine();
            }
        }
        print();

        Action actionInput = null;
        while (sc.hasNext()) {
            try {
                actionInput = Action.valueOf(sc.next().toUpperCase());

                switch (actionInput) {
                    case EXIT:
                        return;
                    case PIECE:
                        if (activePiece != null) System.out.println("Active piece already in play");
                        else {
                            activePiece = getActivePiece();
                            for (int cell : activePiece[variant]) {
                                if (board[cell].equals("0")) {
                                    System.out.println("Game Over!");
                                    return;
                                }
                            }
                            print(activePiece[variant], horizontalOffset, verticalOffset);
                        }
                        break;
                    case DOWN:
                        if (activePiece != null) {
                            boolean isGameOver = moveDownOrFreeze();
                            if (isGameOver) {
                                System.out.println("Game Over!");
                                return;
                            }
                        } else {
                            print();
                        }
                        break;
                    case LEFT:
                        if (activePiece != null) {
                            int checkHorizontal = horizontalOffset;
                            if (calculateLeftmost(activePiece[variant]) + horizontalOffset > 0) checkHorizontal -= 1;
                            if (checkCollisions(activePiece[variant], checkHorizontal, verticalOffset)) {
                                freeze();
                                activePiece = null;
                                print();
                                break;
                            } else {
                                horizontalOffset = checkHorizontal;
                            }
                            moveDownOrFreeze();
                        } else {
                            print();
                        }
                        break;
                    case RIGHT:
                        if (activePiece != null) {

                            int checkHorizontal = horizontalOffset;
                            if (calculateRightmost(activePiece[variant]) + horizontalOffset < width - 1)
                                checkHorizontal += 1;
                            if (checkCollisions(activePiece[variant], checkHorizontal, verticalOffset)) {
                                freeze();
                                activePiece = null;
                                print();
                                break;
                            } else {
                                horizontalOffset = checkHorizontal;

                            }
                            moveDownOrFreeze();
                        } else {
                            print();
                        }
                        break;
                    case ROTATE:
                        if (activePiece != null) {
                            int newVariant = (variant + 1) % activePiece.length;
                            if (calculateRightmost(activePiece[newVariant]) + horizontalOffset < width
                                    && calculateLeftmost(activePiece[newVariant]) + horizontalOffset >= 0
                                    && !(calculateShapeHeight(activePiece[newVariant]) + verticalOffset > height)
                                    && !checkCollisions(activePiece[newVariant], horizontalOffset, verticalOffset)

                            )
                                variant = newVariant;
                            moveDownOrFreeze();
                        } else {
                            print();
                        }
                        break;
                    case BREAK:
                        boolean isFull = true;

                        while (isFull) {
                            for (int i = board.length - width; i < board.length; i++) {
                                if (!board[i].equals("0")) {
                                    isFull = false;
                                    break;
                                }
                            }
                            if (isFull) {
                                for (int i = board.length - 1; i >= 0; i--) {
                                    board[i] = i - width > 0 ? board[i - width] : "-";
                                }
                            }
                        }
                        print();
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input");
            }
        }
    }

    static boolean checkCollisions(Integer[] pieceCells, int checkHorizontal, int checkVertical) {
        boolean isCollision = false;
        for (int cell : pieceCells) {
            int cellCol = (cell + checkHorizontal) % width;
            int cellRow = (cell + checkVertical * width) / width;
            if (Objects.equals(board[cellRow * width + cellCol], "0")) {
                isCollision = true;
                break;
            }

        }
        return isCollision;
    }

    static boolean freeze() {
        boolean isGameOver = false;
        Integer[] pieceCells = activePiece[variant];
        for (int cell : pieceCells) {
            int cellCol = (cell + horizontalOffset) % width;
            int cellRow = (cell + verticalOffset * width) / width;
            if (cellRow == 0) isGameOver = true;
            board[cellRow * width + cellCol] = "0";
        }
        activePiece = null;
        horizontalOffset = 0;
        verticalOffset = 0;
        variant = 0;
        return isGameOver;
    }

    static boolean moveDownOrFreeze() {
        boolean isGameOver = false;
        int checkVertical = verticalOffset + 1;
        Integer[] pieceCells = activePiece[variant];
        if ((calculateShapeHeight(pieceCells) + checkVertical - 1 >= height) || checkCollisions(pieceCells, horizontalOffset, checkVertical)) {
            isGameOver = freeze();
            print();
        } else {
            verticalOffset = checkVertical;
            print(pieceCells, horizontalOffset, verticalOffset);
            if (calculateShapeHeight(pieceCells) + checkVertical >= height) {
                freeze();
            }
        }
        return isGameOver;
    }


    static void print() {
        for (int i = 0; i < board.length; i++) {
            System.out.print(board[i] + " ");
            if ((i + 1) % width == 0) System.out.println();
        }
        System.out.println();
    }

    static void print(Integer[] pieceCells, int horizontalOffset, int verticalOffset) {
        for (int i = 0; i < board.length; i++) {
            int col = i % width;
            int row = i / width;

            boolean isPiece = false;

            for (int cell : pieceCells) {
                int cellCol = (cell + horizontalOffset) % width;
                int cellRow = (cell + verticalOffset * width) / width;
                if (cellCol == col && cellRow == row) {
                    isPiece = true;
                    break;
                }
            }

            System.out.print(isPiece ? "0 " : board[i] + " ");

            if ((i + 1) % width == 0) System.out.println();
        }
        System.out.println();
    }

    static int calculateShapeHeight(Integer[] arr) {
        int height = 0;
        for (int i : arr) {
            int row = i / width;
            if (row > height) height = row;
        }
        return height + 1;
    }

    static int calculateRightmost(Integer[] arr) {
        int right = 0;
        for (int i : arr) {
            int col = i % width;
            if (col > right) right = col;
        }
        return right;
    }

    static int calculateLeftmost(Integer[] arr) {
        int left = width;
        for (int i : arr) {
            int col = i % width;
            if (col < left) left = col;
        }
        return left;
    }

    static Integer[][] getActivePiece() {
        String inputPiece = sc.next().toUpperCase();
        Integer[][] activePiece = rotations.get(inputPiece);
        while (activePiece == null) {
            System.out.println("Invalid input");
            activePiece = rotations.get(sc.next().toUpperCase());
        }
        return activePiece;
    }
}

enum Action {
    ROTATE, LEFT, RIGHT, DOWN, EXIT, PIECE, BREAK
}


