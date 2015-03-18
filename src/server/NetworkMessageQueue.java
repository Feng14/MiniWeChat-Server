package server;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * 存放请求或者回复的队列
 * @author Administrator
 *
 */
public class NetworkMessageQueue implements BlockingQueue<NetworkMessage> {

	@Override
	public NetworkMessage remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkMessage poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkMessage element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkMessage peek() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<NetworkMessage> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends NetworkMessage> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean add(NetworkMessage e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offer(NetworkMessage e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(NetworkMessage e) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean offer(NetworkMessage e, long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NetworkMessage take() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int remainingCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int drainTo(Collection<? super NetworkMessage> c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int drainTo(Collection<? super NetworkMessage> c, int maxElements) {
		// TODO Auto-generated method stub
		return 0;
	}


}
