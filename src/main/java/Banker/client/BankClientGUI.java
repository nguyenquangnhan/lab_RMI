package Banker.client;

import Banker.server.Account;
import Banker.server.AccountList;
import Banker.server.ClientCallback;
import Banker.server.NotificationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;

/**
 * Đây vừa là GIAO DIỆN (JFrame) vừa là MÁY CHỦ CALLBACK (UnicastRemoteObject).
 * PHIÊN BẢN SỬA LỖI DEADLOCK:
 * 1. Dùng SwingWorker cho mọi cuộc gọi RMI từ Client (để giải phóng luồng GUI).
 * 2. Dùng SwingUtilities.invokeLater cho mọi cập nhật GUI từ Callback (để đảm bảo an toàn luồng).
 */
public class BankClientGUI extends JFrame implements ClientCallback {

    // --- Biến RMI ---
    private Registry registry;
    private AccountList accountListRMI;
    private NotificationService notificationServiceRMI;
    private Account currentAccountRMI;
    private ClientCallback clientStub;

    // --- Biến GUI ---
    private JTextArea txtLog;
    private JTextField txtServerIP;
    private JTextField txtAccountLogin;

    private JButton btnLogin;
    private JButton btnCheckBalance;
    private JButton btnDeposit;
    private JButton btnWithdraw;
    private JButton btnTransfer;

    private JPanel panelActions;

    public BankClientGUI() {
        super("VKU Bank Client");

        setupGUI();

        // Cố gắng kết nối RMI ngay khi bật app
        // (Đây là ngoại lệ, làm trên luồng chính để đơn giản)
        try {
            String serverIP = "localhost";
            txtServerIP.setText(serverIP);
            registry = LocateRegistry.getRegistry(serverIP, 1099);
            accountListRMI = (AccountList) registry.lookup("accounts");
            notificationServiceRMI = (NotificationService) registry.lookup("notification");
            log("Kết nối RMI thành công đến " + serverIP);
        } catch (Exception e) {
            log("Lỗi RMI: Không thể kết nối tới server.\n" + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối RMI. Vui lòng kiểm tra IP Server và tường lửa.",
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }

        // Biến Client thành Server (Callback)
        try {
            clientStub = (ClientCallback) UnicastRemoteObject.exportObject(this, 0);
            log("Client Callback đã sẵn sàng.");
        } catch (Exception e) {
            log("Lỗi tạo RMI Callback: " + e.getMessage());
        }

        addListeners();
    }

    /**
     * SỬA LỖI (Phần 1): Gói JOptionPane trong invokeLater
     * Phương thức này được Server gọi (trên luồng RMI).
     * Chúng ta phải dùng SwingUtilities để yêu cầu luồng GUI hiển thị nó.
     */
    @Override
    public void receiveNotification(String message) {
        log("CALLBACK: " + message);

        // Đảm bảo JOptionPane chạy trên luồng GUI
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(BankClientGUI.this, message, "Thông báo từ Server", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void log(String message) {
        // SwingUtilities.invokeLater là bắt buộc để an toàn luồng
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
        });
    }

    /**
     * Phương thức trợ giúp để bật/tắt các nút hành động.
     */
    private void setActionsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnLogin.setEnabled(enabled);
            btnCheckBalance.setEnabled(enabled);
            btnDeposit.setEnabled(enabled);
            btnWithdraw.setEnabled(enabled);
            btnTransfer.setEnabled(enabled);
        });
    }

    private void setupGUI() {
        // ... (Code setupGUI giữ nguyên như cũ) ...
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.add(new JLabel("Server IP:"));
        txtServerIP = new JTextField("localhost", 10);
        panelTop.add(txtServerIP);
        panelTop.add(new JLabel("Số tài khoản:"));
        txtAccountLogin = new JTextField(8);
        panelTop.add(txtAccountLogin);
        btnLogin = new JButton("Đăng nhập");
        panelTop.add(btnLogin);
        add(panelTop, BorderLayout.NORTH);
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtLog);
        add(scrollPane, BorderLayout.CENTER);
        panelActions = new JPanel();
        panelActions.setLayout(new BoxLayout(panelActions, BoxLayout.Y_AXIS));
        btnCheckBalance = new JButton("Vấn tin (Số dư)");
        btnDeposit = new JButton("Nạp tiền");
        btnWithdraw = new JButton("Rút tiền");
        btnTransfer = new JButton("Chuyển khoản");
        panelActions.add(btnCheckBalance);
        panelActions.add(Box.createRigidArea(new Dimension(0, 5)));
        panelActions.add(btnDeposit);
        panelActions.add(Box.createRigidArea(new Dimension(0, 5)));
        panelActions.add(btnWithdraw);
        panelActions.add(Box.createRigidArea(new Dimension(0, 5)));
        panelActions.add(btnTransfer);
        panelActions.setVisible(false);
        add(panelActions, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    private void addListeners() {
        btnLogin.addActionListener(e -> login());
        btnCheckBalance.addActionListener(e -> checkBalance());
        btnDeposit.addActionListener(e -> deposit());
        btnWithdraw.addActionListener(e -> withdraw());
        btnTransfer.addActionListener(e -> transfer());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Hủy đăng ký callback và unexport trước khi thoát
                // (Chạy trong một luồng nền để tránh block luồng GUI)
                new Thread(() -> {
                    if (currentAccountRMI != null && notificationServiceRMI != null && clientStub != null) {
                        try {
                            notificationServiceRMI.unregisterCallback(currentAccountRMI.getNumber(), clientStub);
                            log("Đã hủy đăng ký callback.");
                        } catch (Exception ex) {
                            log("Lỗi khi hủy đăng ký: " + ex.getMessage());
                        }
                    }
                    try {
                        if (clientStub != null) {
                            UnicastRemoteObject.unexportObject(BankClientGUI.this, true);
                        }
                    } catch (Exception ex) {}
                    System.exit(0);
                }).start();
            }
        });
    }

    /**
     * SỬA LỖI (Phần 2): Dùng SwingWorker cho tất cả các hành động.
     * Logic đăng nhập
     */
    private void login() {
        String accNumStr = txtAccountLogin.getText().trim();
        if (accNumStr.isEmpty()) {
            log("Lỗi: Vui lòng nhập số tài khoản.");
            return;
        }

        setActionsEnabled(false); // Vô hiệu hóa nút

        // Định nghĩa công việc trong luồng nền
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private int accNum;

            @Override
            protected Boolean doInBackground() throws Exception {
                accNum = Integer.parseInt(accNumStr);

                // 1. Kiểm tra tài khoản
                boolean found = false;
                for (int a : accountListRMI.getAccounts()) {
                    if (a == accNum) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Số tài khoản " + accNum + " không tồn tại.");
                }

                // 2. Lấy đối tượng Account RMI
                currentAccountRMI = (Account) registry.lookup("act" + accNum);

                // 3. Đăng ký callback
                notificationServiceRMI.registerCallback(accNum, clientStub);
                return true;
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả từ doInBackground
                    if (get()) {
                        log("Đăng nhập thành công tài khoản: " + accNum);
                        log("Đã đăng ký nhận thông báo cho TK " + accNum);
                        panelActions.setVisible(true);
                        btnLogin.setText("Đăng xuất"); // Có thể thêm logic đăng xuất sau
                        txtAccountLogin.setEditable(false);
                    }
                } catch (ExecutionException e) {
                    // Lỗi xảy ra bên trong doInBackground
                    log("Lỗi: " + e.getCause().getMessage());
                } catch (Exception e) {
                    // Lỗi khác
                    log("Lỗi đăng nhập: " + e.getMessage());
                }
                setActionsEnabled(true); // Bật lại nút
            }
        };

        worker.execute(); // Thực thi công việc
    }

    /**
     * Vấn tin dùng SwingWorker
     */
    private void checkBalance() {
        setActionsEnabled(false);
        log("Đang vấn tin số dư...");

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                // RMI call chạy ở luồng nền
                return currentAccountRMI.getBalance();
            }

            @Override
            protected void done() {
                try {
                    double balance = get();
                    log("SỐ DƯ: " + balance);
                } catch (Exception e) {
                    log("Lỗi vấn tin: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Nạp tiền dùng SwingWorker
     */
    private void deposit() {
        String amountStr = JOptionPane.showInputDialog(this, "Nhập số tiền cần nạp:");
        if (amountStr == null) return;

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException nfe) {
            log("Lỗi: Số tiền không hợp lệ.");
            return;
        }

        setActionsEnabled(false);
        log("Đang nạp tiền " + amount + "...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // RMI call chạy ở luồng nền
                return currentAccountRMI.deposit(amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        log("Nạp tiền thành công.");
                        // Không cần gọi checkBalance() ở đây,
                        // vì callback sẽ tự động kích hoạt và cập nhật
                    } else {
                        log("Nạp tiền thất bại.");
                    }
                } catch (Exception e) {
                    log("Lỗi RMI khi nạp tiền: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Rút tiền dùng SwingWorker
     */
    private void withdraw() {
        String amountStr = JOptionPane.showInputDialog(this, "Nhập số tiền cần rút:");
        if (amountStr == null) return;

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException nfe) {
            log("Lỗi: Số tiền không hợp lệ.");
            return;
        }

        setActionsEnabled(false);
        log("Đang rút tiền " + amount + "...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // RMI call chạy ở luồng nền
                return currentAccountRMI.withdraw(amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        log("Rút tiền thành công.");
                    } else {
                        log("Rút tiền thất bại (Không đủ số dư?).");
                    }
                } catch (Exception e) {
                    log("Lỗi RMI khi rút tiền: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Chuyển khoản dùng SwingWorker
     */
    private void transfer() {
        JTextField txtDestAccount = new JTextField(10);
        JTextField txtAmount = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Số tài khoản nhận:"));
        panel.add(txtDestAccount);
        panel.add(new JLabel("Số tiền:"));
        panel.add(txtAmount);

        int result = JOptionPane.showConfirmDialog(this, panel, "Chuyển khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int destAcc;
            double amount;
            try {
                destAcc = Integer.parseInt(txtDestAccount.getText());
                amount = Double.parseDouble(txtAmount.getText());
            } catch (NumberFormatException nfe) {
                log("Lỗi: Số tài khoản hoặc số tiền không hợp lệ.");
                return;
            }

            setActionsEnabled(false);
            log("Đang chuyển " + amount + " đến TK " + destAcc + "...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // RMI call chạy ở luồng nền
                    return currentAccountRMI.transfer(destAcc, amount);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            log("Chuyển khoản thành công.");
                        } else {
                            log("Chuyển khoản thất bại (TK đích, số dư?).");
                        }
                    } catch (Exception e) {
                        log("Lỗi RMI khi chuyển khoản: " + e.getCause().getMessage());
                    }
                    setActionsEnabled(true);
                }
            };
            worker.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankClientGUI().setVisible(true);
        });
    }
}