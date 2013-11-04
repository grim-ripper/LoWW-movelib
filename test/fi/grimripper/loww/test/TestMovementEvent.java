package fi.grimripper.loww.test;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Tile;

public class TestMovementEvent implements MovementEvent {

	private boolean wait = false;
	private float risk = 0f;
	private Height height = null;
	
	private boolean preventsEntry = false;
	private boolean preventsExit = false;
	private boolean showMovementNotAllowed = true;
	
	private boolean buffering = false;
	private boolean bufferOnLeave = false;
	private boolean executeOnce = false;
	
	private MobileObject enteringMob = null;
	private Tile enteredTile = null;
	private Height enteringHeight = null;
	private int enteringCounter = 0;
	
	private MobileObject leavingMob = null;
	private Tile leftTile = null;
	private Height leavingHeight = null;
	private int leavingCounter = 0;
	
	private boolean bufferExecuted = false;
	
	public TestMovementEvent() {
		this( 0f );
	}
	
	public TestMovementEvent( float risk ) {
		this( risk, Height.DEEP, true, true );
	}
	
	public TestMovementEvent( float risk, Height height, boolean noEntry, boolean noExit ) {
		this( risk, height, noEntry, noExit, false );
	}
	
	public TestMovementEvent( float risk, Height height, boolean noEntry, boolean noExit,
			boolean buffering ) {
		this.risk = risk;
		this.height = height;
		preventsEntry = noEntry;
		preventsExit = noExit;
		this.buffering = buffering;
	}
	
	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public Height getHeight( Tile tile ) {
		return height;
	}

	@Override
	public boolean canEnterTile( MobileObject moving, Tile tile, Height height ) {
		enteringMob = moving;
		enteredTile = tile;
		enteringHeight = height;
		
		if (!buffering)
			return !showMovementNotAllowed || !preventsEntry;
		
		MovementMode mode = moving.getMovementMode();
		if (!executeOnce || !mode.isEventBuffered( this, null ))
			mode.addEventToBuffer( this, tile );
		
		bufferExecuted = false;
		return true;
	}

	@Override
	public boolean canLeaveTile( MobileObject moving, Tile tile, Height height ) {
		leavingMob = moving;
		leftTile = tile;
		leavingHeight = height;
		
		if (bufferOnLeave) {
			MovementMode mode = moving.getMovementMode();
			
			if (!mode.isEventBufferExecuting())
				mode.addEventToBuffer( this, tile );
		}
		
		return !showMovementNotAllowed || !preventsExit;
	}

	@Override
	public boolean enteringTile( MobileObject moving, Tile tile, Height height ) {
		if (wait)
			synchronized (this) {
				try {
					notifyAll();
					wait();
				} catch (InterruptedException ix) {
					ix.printStackTrace();
				}
			}
		
		enteringMob = moving;
		enteredTile = tile;
		enteringHeight = height;
		enteringCounter++;
		
		return !preventsEntry;
	}

	@Override
	public boolean leavingTile( MobileObject moving, Tile tile, Height height ) {
		leavingMob = moving;
		leftTile = tile;
		leavingHeight = height;
		leavingCounter++;
		
		return !preventsExit;
	}

	@Override
	public float getRisk( MobileObject moving, Tile tile, Height height ) {
		if (wait)
			synchronized (this) {
				try {
					notifyAll();
					wait();
				} catch (InterruptedException ix) {
					ix.printStackTrace();
				}
			}
		
		enteringMob = moving;
		enteredTile = tile;
		enteringHeight = height;
		
		if (!buffering)
			return risk;
		
		MovementMode mode = moving.getMovementMode();
		if (!mode.isEventBufferExecuting()) {
			if (!executeOnce || !mode.isEventBuffered( this, null ))
				mode.addEventToBuffer( this, tile );
			
			return 0f;
		}
		
		bufferExecuted = true;
		return risk;
	}

	public float getRisk() {
		return risk;
	}
	
	public MobileObject getEnteringObject() {
		return enteringMob;
	}
	
	public Tile getEnteredTile() {
		return enteredTile;
	}
	
	public Height getEnteringHeight() {
		return enteringHeight;
	}
	
	public int getEnteringCounter() {
		return enteringCounter;
	}
	
	public void resetEnteringData() {
		enteringMob = null;
		enteredTile = null;
		enteringHeight = null;
		enteringCounter = 0;
	}
	
	public MobileObject getLeavingObject() {
		return leavingMob;
	}
	
	public Tile getLeftTile() {
		return leftTile;
	}
	
	public Height getLeavingHeight() {
		return leavingHeight;
	}

	public int getLeavingCounter() {
		return leavingCounter;
	}
	
	public void resetLeavingData() {
		leavingMob = null;
		leftTile = null;
		leavingHeight = null;
		leavingCounter = 0;
	}
	
	public void setWaitMode( boolean wait ) {
		this.wait = wait;
	}
	
	public boolean isBufferExecuted() {
		return bufferExecuted;
	}
	
	public void setBufferOnLeave( boolean bufferOnLeave ) {
		this.bufferOnLeave = bufferOnLeave;
	}
	
	public void setExecuteOnce( boolean executeOnce ) {
		this.executeOnce = executeOnce;
	}

	public void setShowMovementNotAllowed( boolean showMovementNotAllowed ) {
		this.showMovementNotAllowed = showMovementNotAllowed;
	}
}