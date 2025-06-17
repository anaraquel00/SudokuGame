import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SudokuGame extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] solution = new int[9][9];
    private int[][] puzzle = new int[9][9];
    private final int EMPTY = 0;
    private final int SIZE = 9;
    private final int SUBGRID = 3;
    private final Color HIGHLIGHT_COLOR = new Color(220, 230, 255);

    public SudokuGame() {
        setTitle("Sudoku Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        
        generateNewPuzzle();
        initializeUI();
    }

    private void generateNewPuzzle() {
        generateSolution(0, 0);
        createPuzzle(40); // Remove 40 números para criar o puzzle
    }

    private boolean generateSolution(int row, int col) {
        if (row == SIZE) {
            row = 0;
            if (++col == SIZE) {
                return true;
            }
        }

        if (solution[row][col] != EMPTY) {
            return generateSolution(row + 1, col);
        }

        int[] nums = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(nums);

        for (int num : nums) {
            if (isValidPlacement(row, col, num)) {
                solution[row][col] = num;
                if (generateSolution(row + 1, col)) {
                    return true;
                }
            }
        }

        solution[row][col] = EMPTY;
        return false;
    }

    private void createPuzzle(int cellsToRemove) {
        // Copia a solução para o puzzle
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, SIZE);
        }

        Random rand = new Random();
        int removed = 0;

        while (removed < cellsToRemove) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);

            if (puzzle[row][col] != EMPTY) {
                puzzle[row][col] = EMPTY;
                removed++;
            }
        }
    }

    private boolean isValidPlacement(int row, int col, int num) {
        // Verifica linha e coluna
        for (int i = 0; i < SIZE; i++) {
            if (solution[row][i] == num || solution[i][col] == num) {
                return false;
            }
        }

        // Verifica subgrade 3x3
        int boxRow = row - row % SUBGRID;
        int boxCol = col - col % SUBGRID;

        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                if (solution[boxRow + i][boxCol + j] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridLayout(9, 9, 1, 1));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        Font font = new Font("Arial", Font.BOLD, 20);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(font);

                // Adiciona bordas para os subgrids 3x3
                if (row % 3 == 0) cells[row][col].setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK));
                if (col % 3 == 0) cells[row][col].setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.BLACK));
                if (row == 8) cells[row][col].setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
                if (col == 8) cells[row][col].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK));

                if (puzzle[row][col] != EMPTY) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(new Color(240, 240, 240));
                } else {
                    cells[row][col].addKeyListener(new SudokuKeyListener(row, col));
                }

                // Destaque ao selecionar
                cells[row][col].addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        highlightRowCol(((JTextField) e.getSource()));
                    }
                });

                panel.add(cells[row][col]);
            }
        }

        JButton checkButton = new JButton("Verificar Solução");
        checkButton.addActionListener(e -> checkSolution());

        JButton newGameButton = new JButton("Novo Jogo");
        newGameButton.addActionListener(e -> {
            generateNewPuzzle();
            resetBoard();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(checkButton);
        buttonPanel.add(newGameButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void highlightRowCol(JTextField source) {
        // Remove destaque anterior
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col].getBackground() == HIGHLIGHT_COLOR) {
                    cells[row][col].setBackground(Color.WHITE);
                }
            }
        }

        // Encontra a célula selecionada
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == source) {
                    // Destaca linha e coluna
                    for (int i = 0; i < SIZE; i++) {
                        cells[row][i].setBackground(HIGHLIGHT_COLOR);
                        cells[i][col].setBackground(HIGHLIGHT_COLOR);
                    }
                    
                    // Destaca subgrade 3x3
                    int boxRow = row - row % SUBGRID;
                    int boxCol = col - col % SUBGRID;
                    
                    for (int i = 0; i < SUBGRID; i++) {
                        for (int j = 0; j < SUBGRID; j++) {
                            cells[boxRow + i][boxCol + j].setBackground(HIGHLIGHT_COLOR);
                        }
                    }
                    
                    source.setBackground(new Color(180, 200, 255)); // Cor mais forte para célula selecionada
                    return;
                }
            }
        }
    }

    private void checkSolution() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Preencha todas as células!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int num = Integer.parseInt(text);
                    if (num < 1 || num > 9 || num != solution[row][col]) {
                        cells[row][col].setBackground(new Color(255, 200, 200));
                        JOptionPane.showMessageDialog(this, "Solução incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    cells[row][col].setBackground(new Color(255, 200, 200));
                    JOptionPane.showMessageDialog(this, "Apenas números de 1 a 9 são permitidos!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Parabéns! Solução correta!", "Vitória", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (puzzle[row][col] != EMPTY) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(new Color(240, 240, 240));
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                    cells[row][col].setBackground(Color.WHITE);
                }
            }
        }
    }

    private class SudokuKeyListener extends KeyAdapter {
        private final int row;
        private final int col;

        public SudokuKeyListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (!(Character.isDigit(c) && c != '0') && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                e.consume(); // Ignora caracteres inválidos
            } else if (Character.isDigit(c)) {
                // Limita a um caractere
                if (((JTextField) e.getSource()).getText().length() >= 1) {
                    e.consume();
                }
            }
        }
    }

}
