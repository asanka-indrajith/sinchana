/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServerUI.java
 *
 * Created on Jan 19, 2012, 9:37:28 PM
 */
package sinchana.test;

import sinchana.chord.FingerTableEntry;

/**
 *
 * @author Hiru
 */
public class ServerUI extends javax.swing.JFrame {

		private StringBuffer sb;
		private boolean serverRunning = true;
		private Tester tester;
//		private Server server;

		/** Creates new form ServerUI */
		public ServerUI() {
//				this.server = server;
				initComponents();
		}

		public ServerUI(Tester t) {
				this();
				tester = t;

		}

		public void setServerId(int id) {
				this.idLabel.setText("Server " + id);
		}

		public void setSuccessorId(int id) {
				this.next.setText("Next: " + id);
		}

		public void setPredecessorId(int id) {
				this.prev.setText("Prev: " + id);
		}

		public void setMessage(String msg) {
				this.message.setText(msg);
		}

		public void setServerRunning(boolean isRunning) {
				this.serverRunning = isRunning;
				if (isRunning) {
						this.down.setText("Stop");
						this.setMessage("Running...");
				} else {
						this.down.setText("Start");
						this.setMessage("Stopped!");
				}
		}

		public void setTableInfo(FingerTableEntry[] fingerTableEntrys) {
				sb = new StringBuffer();
				for (int i = 0; i < fingerTableEntrys.length; i++) {
						sb.append(i).append(":\t");
						sb.append(fingerTableEntrys[i].getStart()).append("  -  ");
						sb.append(fingerTableEntrys[i].getEnd()).append("\t\t");
						sb.append(fingerTableEntrys[i].getSuccessor() != null ? fingerTableEntrys[i].getSuccessor().getServerId() : -1).append("\n");
				}
				display.setText(sb.toString());
		}

		/** This method is called from within the constructor to
		 * initialize the form.
		 * WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {
                java.awt.GridBagConstraints gridBagConstraints;

                jPanel1 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                messageField = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                destField = new javax.swing.JTextField();
                sendButton = new javax.swing.JButton();
                jPanel2 = new javax.swing.JPanel();
                prev = new javax.swing.JLabel();
                idLabel = new javax.swing.JLabel();
                next = new javax.swing.JLabel();
                jScrollPane1 = new javax.swing.JScrollPane();
                display = new javax.swing.JTextArea();
                jPanel3 = new javax.swing.JPanel();
                down = new javax.swing.JButton();
                message = new javax.swing.JLabel();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                getContentPane().setLayout(new java.awt.GridBagLayout());

                jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

                jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
                jLabel1.setText("Send Message");
                jLabel1.setPreferredSize(new java.awt.Dimension(96, 20));
                jPanel1.add(jLabel1);

                messageField.setPreferredSize(new java.awt.Dimension(128, 20));
                jPanel1.add(messageField);

                jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
                jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel2.setText("To");
                jLabel2.setPreferredSize(new java.awt.Dimension(24, 20));
                jPanel1.add(jLabel2);

                destField.setPreferredSize(new java.awt.Dimension(48, 20));
                destField.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                destFieldActionPerformed(evt);
                        }
                });
                jPanel1.add(destField);

                sendButton.setText("Send");
                sendButton.setPreferredSize(new java.awt.Dimension(72, 24));
                sendButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                sendButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(sendButton);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                getContentPane().add(jPanel1, gridBagConstraints);

                jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

                prev.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
                prev.setText("Prev: -1");
                prev.setPreferredSize(new java.awt.Dimension(96, 20));
                jPanel2.add(prev);

                idLabel.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
                idLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                idLabel.setText("000");
                idLabel.setPreferredSize(new java.awt.Dimension(128, 25));
                jPanel2.add(idLabel);

                next.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
                next.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                next.setText("Next: -1");
                next.setPreferredSize(new java.awt.Dimension(96, 20));
                jPanel2.add(next);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                getContentPane().add(jPanel2, gridBagConstraints);

                jScrollPane1.setPreferredSize(new java.awt.Dimension(148, 96));

                display.setColumns(20);
                display.setEditable(false);
                display.setRows(5);
                display.setPreferredSize(new java.awt.Dimension(148, 94));
                jScrollPane1.setViewportView(display);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.ipady = 96;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                getContentPane().add(jScrollPane1, gridBagConstraints);

                jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

                down.setText("Stop");
                down.setPreferredSize(new java.awt.Dimension(72, 24));
                down.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                downActionPerformed(evt);
                        }
                });
                jPanel3.add(down);

                message.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                message.setText("Running...");
                message.setPreferredSize(new java.awt.Dimension(256, 20));
                jPanel3.add(message);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridy = 3;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                getContentPane().add(jPanel3, gridBagConstraints);

                pack();
        }// </editor-fold>//GEN-END:initComponents

private void destFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_destFieldActionPerformed

private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
//		this.server.transferMessage(this.messageField.getText(),
//				Integer.parseInt(this.destField.getText()));
}//GEN-LAST:event_sendButtonActionPerformed

private void downActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downActionPerformed
		if(serverRunning){
				tester.stopServer();
		}else{
				tester.startServer();
		}
}//GEN-LAST:event_downActionPerformed
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JTextField destField;
        private javax.swing.JTextArea display;
        private javax.swing.JButton down;
        private javax.swing.JLabel idLabel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JLabel message;
        private javax.swing.JTextField messageField;
        private javax.swing.JLabel next;
        private javax.swing.JLabel prev;
        private javax.swing.JButton sendButton;
        // End of variables declaration//GEN-END:variables
}
