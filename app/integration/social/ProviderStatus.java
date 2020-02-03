package integration.social;

public class ProviderStatus {
	private boolean supported;
	private boolean connected;
	
	
	public ProviderStatus(boolean supported, boolean connected) {
		this.supported = supported;
		this.connected = connected;
	}
	
	public static final ProviderStatus UNSUPPORTED(){
		return new ProviderStatus(false, false);
	}

	public boolean isSupported() {
		return supported;
	}

	public void setSupported(boolean supported) {
		this.supported = supported;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
