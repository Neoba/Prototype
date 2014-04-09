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

    public Editor() throws IOException, NoSuchAlgorithmException {
        md = MessageDigest.getInstance("MD5");
        initComponents();
        selector = Selector.open();
        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 3000);
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
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DocArea.setColumns(20);
        DocArea.setRows(5);
        jScrollPane1.setViewportView(DocArea);

        syncButton.setText("Sync");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        hashdocLabel.setText("Document: ");

        hashdictLabel.setText("Dictionary: ");

        jLabel3.setText("Neoba Dsync Client");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(syncButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hashdocLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hashdictLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(jLabel3))
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
            hashdocLabel.setText("Document: " + md5hash(md, delta));
        } catch (IOException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_syncButtonActionPerformed
    private class ClientThread extends Thread {

        @Override
        public void run() {
            try {
                ByteBuffer buff = ByteBuffer.allocate(BUFF_SIZE);
                while (sc.read(buff) > 0) {
                    buff.flip();
                }
                age = buff.get();
                int deltasize = buff.getInt();
                if (deltasize > 0) {
                    delta = new byte[deltasize];
                    buff.get(delta, 0, deltasize);
                }
                int dictsize = buff.getInt();
                byte[] dictarr = new byte[dictsize];
                buff.get(dictarr);
                dict = charset.decode(ByteBuffer.wrap(dictarr)).toString();
                if (delta != null) {
                    DocArea.setText(differ.vcdiffDecode(dict, delta));
                } else {
                    DocArea.setText(dict);
                }
                hashdictLabel.setText("Dictionary: " + md5hash(md, dict.getBytes()));
                hashdocLabel.setText("Document: " + md5hash(md, new byte[]{}));
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
                            hashdocLabel.setText("Document: " + md5hash(md, delta));
                            System.out.println("Recieved: " + content);
                            age += 1;
                            if (age > ROLL_FWD_COUNT) {
                                dict = content;
                                age = 0;
                                hashdictLabel.setText("Dictionary: " + md5hash(md, dict.getBytes()));

                            }
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables
}
