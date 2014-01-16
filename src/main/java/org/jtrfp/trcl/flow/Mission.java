package org.jtrfp.trcl.flow;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.BackdropSystem;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.TunnelInstaller;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.obj.DebrisFactory;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;

public class Mission {
    private final TR tr;
    private final List<NAVObjective> navs= new LinkedList<NAVObjective>();
    private final LVLFile lvl;
    private final Object missionCompleteBarrier = new Object();
    private final HashMap<String,Tunnel> tunnels = new HashMap<String,Tunnel>();
    private Vector3D playerStartPosition;
    private ObjectDirection playerStartDirection;
    public Mission(TR tr, LVLFile lvl){
	this.tr=tr;
	this.lvl=lvl;
    }//end Mission
    
    public Result go(){
	try{
	//Set up palette
    	final ResourceManager rm = tr.getResourceManager();
    	final Color [] pal = rm.getPalette(lvl.getGlobalPaletteFile());
    	pal[0]=new Color(0,0,0,0);
    	tr.setGlobalPalette(pal);
	// POWERUPS
	rm.setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
	/// EXPLOSIONS
	rm.setExplosionFactory(new ExplosionFactory(tr));
	// DEBRIS
	rm.setDebrisFactory(new DebrisFactory(tr));
	//SETUP PROJECTILE FACTORIES
	Weapon [] w = Weapon.values();
	ProjectileFactory [] pf = new ProjectileFactory[w.length];
	for(int i=0; i<w.length;i++){
	    pf[i]=new ProjectileFactory(tr, w[i], ExplosionType.Blast);
	}//end for(weapons)
	rm.setProjectileFactories(pf);
	Player player =new Player(tr,tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl())); 
	tr.setPlayer(player);
	final String startX=System.getProperty("org.jtrfp.trcl.startX");
	final String startY=System.getProperty("org.jtrfp.trcl.startY");
	final String startZ=System.getProperty("org.jtrfp.trcl.startZ");
	if(startX!=null && startY!=null&&startZ!=null){
	    System.out.println("Using user-specified start point");
	    final int sX=Integer.parseInt(startX);
	    final int sY=Integer.parseInt(startY);
	    final int sZ=Integer.parseInt(startZ);
	    player.setPosition(new Vector3D(sX,sY,sZ));
	}
	tr.getWorld().add(player);
	final TDFFile tdf = rm.getTDFData(lvl.getTunnelDefinitionFile());
	tr.setOverworldSystem(new OverworldSystem(tr.getWorld(), lvl, tdf));
	final NAVSystem navSystem = tr.getNavSystem();
	
	List<NAVSubObject> navSubObjects = rm.getNAVData(lvl.getNavigationFile()).getNavObjects();
	START s = (START)navSubObjects.get(0);
	navSubObjects.remove(0);
	Location3D l3d = s.getLocationOnMap();
	playerStartPosition = new Vector3D(TR.legacy2Modern(l3d.getZ()),TR.legacy2Modern(l3d.getY()),TR.legacy2Modern(l3d.getX()));
	playerStartDirection = new ObjectDirection(s.getRoll(),s.getPitch(),s.getYaw());
	TunnelInstaller tunnelInstaller = new TunnelInstaller(tdf,tr.getWorld());
	//Install NAVs
	for(NAVSubObject obj:navSubObjects){
	    NAVObjective.create(tr, obj, tr.getOverworldSystem().getDefList(), navs, tr.getOverworldSystem());
	}//end for(navSubObjects)
	navSystem.updateNAVState();
	tr.setBackdropSystem(new BackdropSystem(tr.getWorld()));

	//////// INITIAL HEADING
	tr.getPlayer().setPosition(tr.getCurrentMission().getPlayerStartPosition());
	tr.getPlayer().setDirection(tr.getCurrentMission().getPlayerStartDirection());
	tr.getPlayer().setHeading(tr.getPlayer().getHeading().negate());//Kludge to fix incorrect heading
	System.out.println("Start position set to "+tr.getPlayer().getPosition());
	
	
	GPU gpu = tr.getGPU();
	//gpu.takeGL();//Remove if tunnels are put back in. TunnelInstaller takes the GL for us.
	System.out.println("Building master texture...");
	Texture.finalize(gpu);
	System.out.println("\t...Done.");
	System.out.println("Finalizing GPU memory allocation...");
	GlobalDynamicTextureBuffer.finalizeAllocation(gpu);
	gpu.releaseGL();
	//////// NO GL BEYOND THIS POINT ////////
	System.out.println("\t...Done.");
	System.out.println("Invoking JVM's garbage collector...");
	System.gc();
	System.out.println("\t...Ahh, that felt good.");
	System.out.println("Attaching to GL Canvas...");
	
	System.out.println("\t...Done.");
	System.out.println("Starting animator...");
	tr.getThreadManager().start();
	System.out.println("\t...Done.");
	}catch(Exception e){e.printStackTrace();}
	return new Result(null);//TODO: Replace null with actual value unless end of game.
    }//end go()
    
    public NAVObjective currentNAVTarget(){
	if(navs.isEmpty())return null;
	return navs.get(0);
    }
    public void removeNAVObjective(NAVObjective o){
	navs.remove(o);
	if(navs.size()==0){missionCompleteSequence();}
	else tr.getNavSystem().updateNAVState();
    }//end removeNAVObjective(...)
    
    public class Result{
	private final String nextLVL;
	public Result(String nextLVL){
	    this.nextLVL=nextLVL;
	}
	public String getNextLVL() {
	    return nextLVL;
	}
    }//end Result

    /**
     * @return the playerStartPosition
     */
    public Vector3D getPlayerStartPosition() {
        return playerStartPosition;
    }

    /**
     * @return the playerStartDirection
     */
    public ObjectDirection getPlayerStartDirection() {
        return playerStartDirection;
    }

    public Tunnel newTunnel(org.jtrfp.trcl.file.TDFFile.Tunnel tun) {
	final Tunnel result = new Tunnel(tr.getWorld(),tun);
	tunnels.put(tun.getTunnelLVLFile().toUpperCase(), result);
	return result;
    }

    public Tunnel getTunnelByFileName(String tunnelFileName) {
	return tunnels.get(tunnelFileName.toUpperCase());
    }
    
    private void missionCompleteSequence(){
	new Thread(){
	    @Override
	    public void run(){
		//TODO: Behavior change: Camera XZ static, lag Y by ~16 squares, heading/top affix toward player
		//TODO: Turn off all player control behavior
		//TODO: Behavior change: Player turns upward, top rolls on heading, speed at full throttle
		//TODO: Wait 3 seconds
		//TODO: Lightning shell on
		//TODO: Wait 1 second
		//TODO: Turbo forward
		//TODO: Wait 500ms
		//TODO: Jet thrust noise
		//TODO: Player invisible.
		tr.getGame().missionComplete();
	    }//end run()
	}.start();
    }
    public void playerDestroyed(){
	new Thread(){
	    @Override
	    public void run(){
		//TODO Behavior change: Camera XYZ static, heading/top affix toward player
		//TODO: Turn off all player control behavior
		//TODO Player behavior change: Slow spin along heading axis, slow downward drift of heading
		//TODO: Add behavior: explode and destroy on impact with ground
		tr.getGame().missionFailed();
	    }//end run()
	}.start();
	
    }//end playerDestroyed()
}//end Mission
