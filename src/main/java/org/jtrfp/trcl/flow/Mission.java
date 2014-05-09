package org.jtrfp.trcl.flow;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.BackdropSystem;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.TunnelInstaller;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.AbstractVector;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.NAVObjective.Factory;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.DebrisFactory;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeFactory;

public class Mission {
    private final TR tr;
    private final List<NAVObjective> navs= new LinkedList<NAVObjective>();
    private final LVLFile lvl;
    private final Object missionCompleteBarrier = new Object();
    private final HashMap<String,Tunnel> tunnels = new HashMap<String,Tunnel>();
    private double [] playerStartPosition=  new double[3];
    private List<NAVSubObject> navSubObjects;
    private ObjectDirection playerStartDirection;
    public Mission(TR tr, LVLFile lvl){
	this.tr=tr;
	this.lvl=lvl;
    }//end Mission
    
    public Result go(){
	try{
	//Set up palette
    	final ResourceManager rm = tr.getResourceManager();
    	final ThreadManager tm = tr.getThreadManager();
    	final Color [] pal = rm.getPalette(lvl.getGlobalPaletteFile());
	System.out.println("\t...Done.");
    	pal[0]=new Color(0,0,0,0);
    	tr.setGlobalPalette(pal);

	// POWERUPS
	rm.setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
	/// EXPLOSIONS
	rm.setExplosionFactory(new ExplosionFactory(tr));
	// SMOKE
	rm.setSmokeFactory(new SmokeFactory(tr));
	// DEBRIS
	rm.setDebrisFactory(new DebrisFactory(tr));
	
	//SETUP PROJECTILE FACTORIES
		Weapon [] w = Weapon.values();
		ProjectileFactory [] pf = new ProjectileFactory[w.length];
		for(int i=0; i<w.length;i++){
		    pf[i]=new ProjectileFactory(tr, w[i], ExplosionType.Blast);
		}//end for(weapons)
		rm.setProjectileFactories(pf);
		final Player player =new Player(tr,rm.getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.gpu.get().getGl())); 
		final String startX=System.getProperty("org.jtrfp.trcl.startX");
		final String startY=System.getProperty("org.jtrfp.trcl.startY");
		final String startZ=System.getProperty("org.jtrfp.trcl.startZ");
		final double [] playerPos = player.getPosition();
		if(startX!=null && startY!=null&&startZ!=null){
		    System.out.println("Using user-specified start point");
		    final int sX=Integer.parseInt(startX);
		    final int sY=Integer.parseInt(startY);
		    final int sZ=Integer.parseInt(startZ);
		    playerPos[0]=sX;
		    playerPos[1]=sY;
		    playerPos[2]=sZ;
		    player.notifyPositionChange();
		}//end if(start!=null)
    	
	final World world = tr.getWorld();
	final TDFFile tdf = rm.getTDFData(lvl.getTunnelDefinitionFile());
	tr.setOverworldSystem(new OverworldSystem(world));
	tr.setPlayer(player);
	world.add(player);
	tr.getOverworldSystem().loadLevel(lvl, tdf);
	
    		//Install NAVs
    		final NAVSystem navSystem = tr.getNavSystem();
    		navSubObjects = rm.getNAVData(lvl.getNavigationFile()).getNavObjects();
    		
    		START s = (START)navSubObjects.get(0);
    		navSubObjects.remove(0);
    		Location3D l3d = s.getLocationOnMap();
    		playerStartPosition[0]=TR.legacy2Modern(l3d.getZ());
    		playerStartPosition[1]=TR.legacy2Modern(l3d.getY());
    		playerStartPosition[2]=TR.legacy2Modern(l3d.getX());
    		playerStartDirection = new ObjectDirection(s.getRoll(),s.getPitch(),s.getYaw());
    		
    		TunnelInstaller tunnelInstaller = new TunnelInstaller(tdf,world);
    		Factory f = new NAVObjective.Factory(tr);
    		for(NAVSubObject obj:navSubObjects){
    		    f.create(tr, obj, navs);
    		}//end for(navSubObjects)
    		navSystem.updateNAVState();
    		tr.setBackdropSystem(new BackdropSystem(world));
    	
    		//////// INITIAL HEADING
    		player.setPosition(getPlayerStartPosition());
    		player.setDirection(getPlayerStartDirection());
    		player.setHeading(player.getHeading().negate());//Kludge to fix incorrect heading
    		System.out.println("Start position set to "+player.getPosition());
    		
    		GPU gpu = tr.gpu.get();
    		//gpu.takeGL();//Remove if tunnels are put back in. TunnelInstaller takes the GL for us.
    		System.out.println("Building atlas texture...");
    		Texture.finalize(gpu);
    		System.out.println("Setting sun vector");
    		final AbstractVector sunVector = lvl.getSunlightDirectionVector();
    		tr.getThreadManager().submitToGL(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			tr.renderer.get().setSunVector(new Vector3D(sunVector.getX(),-sunVector.getY(),sunVector.getZ()).normalize());
			return null;
		    }
    		}).get();
    		System.out.println("\t...Done.");
    	
	//////// NO GL BEYOND THIS POINT ////////
	System.out.println("\t...Done.");
	System.out.println("Invoking JVM's garbage collector...");
	System.gc();
	System.out.println("\t...Ahh, that felt good.");
	System.out.println("Activating renderer...");
	tr.renderer.get().activate();
	System.out.println("\t...Done.");
	
	}catch(Exception e){e.printStackTrace();}
	if(System.getProperties().containsKey("org.jtrfp.trcl.flow.Mission.skipNavs")){
	    try{
	    final int skips = Integer.parseInt(System.getProperty("org.jtrfp.trcl.flow.Mission.skipNavs"));
	    System.out.println("Skipping "+skips+" navs.");
	    for(int i=0; i<skips; i++){
		removeNAVObjective(currentNAVObjective());
	    }//end for(skips)
	    }catch(NumberFormatException e){
		System.err.println("Invalid format for property \"org.jtrfp.trcl.flow.Mission.skipNavs\". Must be integer.");}
	}//end if(containsKey)
	System.out.println("Mission.go() complete.");
	return new Result(null);//TODO: Replace null with actual value unless end of game.
    }//end go()
    
    public NAVObjective currentNAVObjective(){
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
    public double[] getPlayerStartPosition() {
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
    public Tunnel getTunnelWhoseEntranceClosestTo(double xInLegacyUnits, double yInLegacyUnits, double zInLegacyUnits){
	Tunnel result=null; 
	double closestDistance=Double.POSITIVE_INFINITY;
	for(Tunnel t:tunnels.values()){
	    TDFFile.Tunnel src =t.getSourceTunnel();
	    final double distance=Math.sqrt(
		    Math.pow((xInLegacyUnits-src.getEntrance().getX()),2)+
		    Math.pow((yInLegacyUnits-src.getEntrance().getY()),2)+
		    Math.pow((zInLegacyUnits-src.getEntrance().getZ()),2));
	    if(distance<closestDistance){closestDistance=distance;result=t;}
	}//end for(tunnels)
	return result;
    }//end getTunnelWhoseEntranceClosestTo(...)
    
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

    public List<NAVObjective> getRemainingNAVObjectives() {
	return navs;
    }

    /**
     * @return the navSubObjects
     */
    public List<NAVSubObject> getNavSubObjects() {
        return navSubObjects;
    }

    /**
     * @param navSubObjects the navSubObjects to set
     */
    public void setNavSubObjects(List<NAVSubObject> navSubObjects) {
        this.navSubObjects = navSubObjects;
    }
}//end Mission
