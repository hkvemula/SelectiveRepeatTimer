package com.srtimer;

import java.io.IOException;
import java.net.*;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.srtimer.ConnectionbtwThread.OnReceivedListener;

public class PacketSender extends ConnectionbtwThread {
	private InetAddress mAddrReciever;
	private DatagramSocket mSocket;
	int PORT_SENDER = 3049;
	int PORT_RECEIVER = 4389;
	int SIZE_PACKET = 1024;
	private byte[] mQuickBuffer = new byte[SIZE_PACKET];
	private SRTimer SR = null;

	public PacketSender(SRTimer parentContext) throws SocketException, UnknownHostException {
		super();
		SR = parentContext;
		mAddrReciever = InetAddress.getLocalHost();
		mSocket = new DatagramSocket(PORT_SENDER);
	}

	/**
	 * Run method is used to start the process of sending the packet to receiver
	 * from sender.
	 */

	public void run() {

		initListen(new OnReceivedListener() {
			@Override
			public void onReceived() throws Exception {

				mSocket.receive(new DatagramPacket(mQuickBuffer, mQuickBuffer.length));
				Packet packet = (Packet) deserialization(mQuickBuffer);
				if (packet.isAck()) {
					SR.updateBufferSlotTimerSender(packet.getSeq(), BufferSlotTimer.ACKED);
					SR.output.append("(S) - Acknowledgment for Packet " + packet.getSeq() + " has been Received \n");
					SR.updateBaseSnd();
					SR.getBufferSlotTimerSender(packet.getSeq()).stopTimerTimeout();
					SR.output.append("(S) - Timer for Packet " + packet.getSeq() + " has been stopped \n");
				}
			}
		});
	}

	/**
	 * fakeSend method
	 */
	@Override
	protected void fakeSend(int seq) throws IOException {
		SR.updateBufferSlotTimerSender(seq, BufferSlotTimer.SENT);
		SR.updateNextSequenceSnd();
		SR.getFlyingPart(seq).setY(0);
		SR.getFlyingPart(seq).setAck(false);
		SR.getFlyingPart(seq).setVisible(true);
		if (SR.getBufferSlotTimerSender(seq).getFasterState() == true) {
			SR.getBufferSlotTimerSender(seq).setAnimation(100 / 2);
			System.out.println(seq + "hello");
		}
		SR.getBufferSlotTimerSender(seq).startTimerAnimation(new TimerTask() {
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {

				++times;
				if (times > timesMax / 2) {
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (distStep * times));
				SR.repaint();
			}
		});
		SR.getBufferSlotTimerSender(seq).startTimerTimeout(new TimerTask() {
			@Override
			public void run() {

				try {
					SR.retransmit();
				} catch (IOException e) {
					System.out.println("Fake send Process failed at packet sender: " +e);
				}
			}
		});
	}

	/**
	 * This method displays the track of packet sent in the text box.
	 */

	@Override
	protected void sendSuccess(int seq) throws IOException {
		SR.updateBufferSlotTimerSender(seq, BufferSlotTimer.SENT);
		SR.output.append("(S) - Packet " + seq + " SENT  \n");
		SR.updateNextSequenceSnd();
		SR.getFlyingPart(seq).setY(0);
		SR.getFlyingPart(seq).setAck(false);
		SR.getFlyingPart(seq).setVisible(true);
		SR.getBufferSlotTimerSender(seq).startTimerAnimation(new TimerTask() {

			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {

				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(false, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrReciever,
								PORT_RECEIVER));
					} catch (IOException e) {
						e.printStackTrace();
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (distStep * times));
				SR.repaint();
			}
		});
		SR.getBufferSlotTimerSender(seq).startTimerTimeout(new TimerTask() {
			@Override
			public void run() {

				try {
					SR.retransmit();
				} catch (IOException e) {
					System.out.println("Run process failed at packet sender: " +e);
				}
			}
		});
	}

	/**
	 * kill method is used to kill the selected packet and we use code Part from
	 * here.
	 */

	public void kill(int seq) throws IOException {
		try {
			Thread.sleep(300);
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketSender.class.getName()).log(Level.SEVERE, null, ex);
		}
		SR.getFlyingPart(seq).setSelected(false);
		SR.getFlyingPart(seq).setY(0);
		SR.getFlyingPart(seq).setAck(false);
		SR.getFlyingPart(seq).setVisible(false);
		SR.getBufferSlotTimerReceiver(seq).startTimerTimeout(new TimerTask() {
			@Override
			public void run() {

				try {
					SR.retransmit();
				} catch (IOException e) {
					System.out.println("Kill process failedat packet sender: " +e);
				}
			}
		});

		SR.repaint();
	}

	/**
	 * Pause method is used to pause the packet which is being sent between sender
	 * and receiver.
	 */
	public void pause(int seq) throws IOException {
		SR.getFlyingPart(seq).setSelected(true);
		SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
		SR.repaint();
	}

	/**
	 * Resume and Pause both the methods does same process.
	 */

	public void resume(int seq) throws IOException {
		SR.getFlyingPart(seq).setSelected(false);
		SR.getBufferSlotTimerSender(seq).startTimerAnimation(new TimerTask() {
			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(false, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrReciever,
								PORT_RECEIVER));
					} catch (IOException e) {
						e.printStackTrace();
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (distStep * times));
				SR.repaint();
			}
		});
	}

	public void slower(int seq) {
		SR.getBufferSlotTimerSender(seq).setAnimation(100 * 2);
		SR.getBufferSlotTimerSender(seq).startTimerAnimation(new TimerTask() {

			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(false, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrReciever,
								PORT_RECEIVER));
					} catch (IOException e) {
						e.printStackTrace();
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (distStep * times));
				SR.repaint();
			}
		});
	}

	public void faster(int seq) {
		SR.getBufferSlotTimerSender(seq).setAnimation(100 / 4);
		SR.getBufferSlotTimerSender(seq).startTimerAnimation(new TimerTask() {

			int times = 0;
			private final int timesMax = (BufferSlotTimer.SEC_DELIVERY * 1000) / BufferSlotTimer.MSEC_ANIM;
			private final double distStep = SR.DISTANCE_BETWEEN_BUFFERS / timesMax;

			@Override
			public void run() {
				++times;
				if (times > timesMax) {
					try {
						byte[] serializedMessage = serialization(new Packet(false, seq));
						mSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, mAddrReciever,
								PORT_RECEIVER));
					} catch (IOException e) {
						e.printStackTrace();
					}
					SR.getFlyingPart(seq).setVisible(false);
					SR.repaint();
					SR.getBufferSlotTimerSender(seq).stopTimerAnimation();
					return;
				}
				SR.getFlyingPart(seq).setY((int) (distStep * times));
				SR.repaint();
			}
		});
	}

	@Override
	public void reset(int seq) throws IOException {
		for (int i = 0; i < seq - 1; ++i) {
			SR.updateBufferSlotTimerSender(i, BufferSlotTimer.EMPTY);
			SR.getFlyingPart(i).setY(0);
			SR.getFlyingPart(i).setAck(false);
			SR.getFlyingPart(i).setVisible(false);
			SR.getBufferSlotTimerSender(i).mTimerAnimation = null;
			SR.getBufferSlotTimerSender(i).stopTimerAnimation();
			SR.repaint();
		}
	}
}
