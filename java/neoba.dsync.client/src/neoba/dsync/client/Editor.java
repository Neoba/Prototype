package neoba.dsync.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import openvcdiffjava.Openvcdiffjava;

public class Editor extends javax.swing.JFrame {

    private final Selector selector;
    private final Charset charset = Charset.forName("UTF-8");
    private SocketChannel sc = null;
    private String dict = " ";
    private Openvcdiffjava differ = new Openvcdiffjava();
    private byte age = 5;
    private final byte ROLL_FWD_COUNT = 5;
    private final int BUFF_SIZE = 10000;
    private final MessageDigest md;
    byte[] delta = null;
    private String username;
    private String ip;

    public Editor() throws IOException, NoSuchAlgorithmException {
        this.setTitle("Neoba Dsync Client");
        md = MessageDigest.getInstance("MD5");
        initComponents();
        selector = Selector.open();
        username = JOptionPane.showInputDialog("Enter username");
        ip = JOptionPane.showInputDialog("Enter server address");
        InetSocketAddress isa = new InetSocketAddress(ip.equals("") ? "127.0.0.1" : ip, 3000);
        sc = SocketChannel.open(isa);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        new ClientThread().start();

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        DocArea = new javax.swing.JTextArea();
        syncButton = new javax.swing.JButton();
        hashdocLabel = new javax.swing.JLabel();
        hashdictLabel = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DocArea.setColumns(20);
        DocArea.setRows(5);
        DocArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                DocAreaKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(DocArea);

        syncButton.setText("Sync");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        hashdocLabel.setText("Document: ");

        hashdictLabel.setText("Dictionary: ");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton1.setText("Create");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hashdocLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hashdictLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hashdictLabel)
                    .addComponent(hashdocLabel)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        try {
            delta = differ.vcdiffEncode(dict, DocArea.getText());
            ByteBuffer buf = ByteBuffer.wrap(delta);
            sc.write(buf);
            hashdocLabel.setText("Document: " + md5hash(md, DocArea.getText().getBytes()));
            ageInc(DocArea.getText());
        } catch (IOException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_syncButtonActionPerformed

    private void DocAreaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DocAreaKeyTyped
        hashdocLabel.setText("Document: " + md5hash(md, DocArea.getText().getBytes()));
    }//GEN-LAST:event_DocAreaKeyTyped
    private class ClientThread extends Thread {

        @Override
        public void run() {
            try {
                ByteBuffer buff = ByteBuffer.allocate(BUFF_SIZE);
                while (sc.read(buff) > 0) {
                    buff.flip();
                }
                WelcomeMessageReader wmr = new WelcomeMessageReader(buff);
                delta = wmr.getDelta();
                dict = wmr.getDict();
                age = wmr.getAge();
                if (delta != null) {
                    DocArea.setText(differ.vcdiffDecode(dict, delta));
                } else {
                    DocArea.setText(dict);
                }
                hashdictLabel.setText("Dictionary: " + md5hash(md, dict.getBytes()));
                hashdocLabel.setText("Document: " + md5hash(md, DocArea.getText().getBytes()));
                while (selector.select() > 0) {
                    for (SelectionKey sk : selector.selectedKeys()) {
                        selector.selectedKeys().remove(sk);
                        if (sk.isReadable()) {
                            SocketChannel sc = (SocketChannel) sk.channel();
                            buff = ByteBuffer.allocate(BUFF_SIZE);
                            int count = 0, size = 0;
                            while ((size = sc.read(buff)) > 0) {
                                count += size;
                                buff.flip();
                            }
                            delta = new byte[count];
                            for (int i = 0; i < count; i++) {
                                delta[i] = buff.get(i);
                            }
                            String content = differ.vcdiffDecode(dict, delta);
                            DocArea.setText(content);
                            hashdocLabel.setText("Document: " + md5hash(md, DocArea.getText().getBytes()));
                            System.out.println("Recieved: " + content);
                            ageInc(content);
                            sk.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String md5hash(MessageDigest md, byte[] arr) {
        byte[] hash = md.digest(arr);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString().substring(0, 8);
    }

    private void ageInc(String content) {
        age += 1;
        if (age > ROLL_FWD_COUNT) {
            dict = content;
            age = 0;
            hashdictLabel.setText("Dictionary: " + md5hash(md, dict.getBytes()));

        }
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Editor().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea DocArea;
    private javax.swing.JLabel hashdictLabel;
    private javax.swing.JLabel hashdocLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables
}
