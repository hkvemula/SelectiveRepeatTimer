package com.srtimer;
import java.io.IOException;
import java.net.*;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.srtimer.ConnectionbtwThread.OnReceivedListener;

public class PacketReceiver extends ConnectionbtwThread {
	private InetAddress mAddrSender;
	private DatagramSocket mSocket;
	int PORT_SENDER = 3049;
	int PORT_RECEIVER = 4389;
	int SIZE_PACKET = 1024;
	private byte[] mQuickBuffer = new byte[SIZE_PACKET];
	private SRTimer SR = null;

	public PacketReceiver(SRTimer parentContext) throws UnknownHostException, SocketException {
		super();
		SR = parentContext;
		mAddrSender = InetAddress.getLocalHost();
		mSocket = new DatagramSocket(PORT_RECEIVER);
	}

	@Override
	public void run() {
		initListen(new OnReceivedListener() {
			@Override
			public void onReceived() throws Exception {
				
				mSocket.receive(new DatagramPacket(mQuickBuffer, mQuickBuffer.length));
				Packet packet = (Packet) deserialization(mQuickBuffer);
				if (!packet.isAck()) {
					SR.updateBufferSlotTimerReceiver(packet.getSeq(), BufferSlotTimer.BUFFERED);
					SR.output.append("(R) - Packet " + packet.getSeq() + " BUFFERED \n ");
					SR.updateBaseRcv();
					send(packet.getSeq());
				}
			}
		});
	}

	/**
	 * Timer Animation starts and we use code Part from here.
	 */
	@Override
	protected void fakeSend(int seq) {
		SR.getFlyingPart(seq).setY(SR.DISTANCE_BETWEEN_BUFFERS);
		SR.getFlyingPart(seq).setAck(true);
		SR.getFlyingPart(seq).setVisible(true);
		SR.getBufferSlotTimerReceiver(seq).startTimerAnimation(new TimerTask() {
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			/**
			 * Timer Animation stops
			 */
			public void run() {
				++times;
				if (times > timesMax / 2) {
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
					return;
				} // if
				SR.getFlyingPart(seq).setY((int) (SR.DISTANCE_BETWEEN_BUFFERS - (distStep * times)));
				SR.repaint();
			}
		});
	}

	@Override
	protected void sendSuccess(int seq) throws IOException {
		SR.getFlyingPart(seq).setY(SR.DISTANCE_BETWEEN_BUFFERS);
		SR.output.append("(R) - Packet " + seq + " has been Received \n");
		SR.getFlyingPart(seq).setAck(true);
		SR.getFlyingPart(seq).setVisible(true);
		SR.output.append("(R) - Packet " + seq + " is on the way for Acknowledgment \n");
		SR.getBufferSlotTimerReceiver(seq).startTimerAnimation(new TimerTask() { // Start Time Animation function
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			/**
			 * Run method is used to start the process of sending the packet to receiver
			 * from sender.
			 */
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(true, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrSender,
								PORT_SENDER));
					} catch (IOException e) {
						System.out.println("Run process failed at packet receiver class " +e);
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
					return;
				} 
				SR.getFlyingPart(seq).setY((int) (SR.DISTANCE_BETWEEN_BUFFERS - (distStep * times)));
				SR.repaint();
			}
		});
	}

	/**
	 * Reset method is used to reset the changes done to the SRTimer.
	 */
	public void reset(int seq) throws IOException {
		SR.getFlyingPart(seq).setY(0);
		SR.getFlyingPart(seq).setAck(false);
		SR.getFlyingPart(seq).setVisible(false);
		SR.getBufferSlotTimerReceiver(seq).mTimerAnimation = null;
		SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
		SR.repaint();
	}

	/**
	 * In this method the process of sending packets is done slowly.
	 * 
	 * @param seq
	 */
	public void slower(int seq) {
		SR.getFlyingPart(seq).setY(SR.DISTANCE_BETWEEN_BUFFERS);
		SR.getFlyingPart(seq).setAck(true);
		SR.getFlyingPart(seq).setVisible(true);
		SR.getBufferSlotTimerReceiver(seq).setAnimation(100 * 2);
		SR.getBufferSlotTimerReceiver(seq).startTimerAnimation(new TimerTask() {
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(true, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrSender,
								PORT_SENDER));
					} catch (IOException e) {
						System.out.println("slow process failed at packet Receiver class: " +e);
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (SR.DISTANCE_BETWEEN_BUFFERS - (distStep * times)));
				SR.repaint();
			}
		});
	}

	/**
	 * In this method the process of sending packets is done faster.
	 * 
	 * @param seq
	 */
	public void faster(int seq) {
		SR.getBufferSlotTimerReceiver(seq).setAnimation(100 / 2);
		SR.getBufferSlotTimerReceiver(seq).startTimerAnimation(new TimerTask() {
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(true, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrSender,
								PORT_SENDER));
					} catch (IOException e) {
						System.out.println("faster process failed at packet Receiver class: " +e);
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (SR.DISTANCE_BETWEEN_BUFFERS - (distStep * times)));
				SR.repaint();
			}
		});
	}

	/**
	 * kill method is used to kill the selected packet.
	 */
	public void kill(int seq) throws IOException {
		SR.getFlyingPart(seq).setSelected(true);
		try {
			Thread.sleep(300);
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketReceiver.class.getName()).log(Level.SEVERE, null, ex);
		}
		SR.getBufferSlotTimerReceiver(seq).setState(BufferSlotTimer.EMPTY);
		SR.getFlyingPart(seq).setY(0);
		SR.getFlyingPart(seq).setAck(false);
		SR.getFlyingPart(seq).setVisible(false);
		SR.getBufferSlotTimerSender(seq).startTimerTimeout(new TimerTask() {
			@Override
			public void run() {
				try {
					SR.retransmit();
				} catch (IOException e) {
					System.out.println("Kill process failed at packet Receiver class: " +e);
				}
			}
		});
		SR.repaint();
	}

	/**
	 * Pause method is used to pause the packet which is being sent between sender
	 * and receiver.
	 */
	@Override
	public void pause(int seq) throws IOException {
		SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
		SR.repaint(); 
	}

	/**
	 * Resume and Pause both the methods does same process.
	 */
	public void resume(int seq) throws IOException {
		SR.getBufferSlotTimerReceiver(seq).startTimerAnimation(new TimerTask() { // Stop Time Animation function
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(true, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrSender,
								PORT_SENDER));
					} catch (IOException e) {
						e.printStackTrace();
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerReceiver(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (SR.DISTANCE_BETWEEN_BUFFERS - (distStep * times)));
				SR.repaint();
			}
		});
	}

}
