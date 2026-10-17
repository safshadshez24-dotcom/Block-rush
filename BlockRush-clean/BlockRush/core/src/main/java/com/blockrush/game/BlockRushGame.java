package com.blockrush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/** First playable BLOCK RUSH slice. All geometry is generated from primitives. */
public final class BlockRushGame extends ApplicationAdapter {
    private static final float[] LANES = {-2.2f, 0f, 2.2f};
    private final Array<Thing> things = new Array<>();
    private final Array<ModelInstance> track = new Array<>();
    private ModelBatch modelBatch;
    private SpriteBatch hudBatch;
    private BitmapFont font;
    private PerspectiveCamera camera;
    private OrthographicCamera hudCamera;
    private Environment environment;
    private Model floorModel, playerModel, crateModel, beamModel, coinModel;
    private ModelInstance playerInstance;
    private int lane, coins, best;
    private float playerX, jumpY, jumpVelocity, slideTimer;
    private float elapsed, distance, speed, spawnTimer, trackOffset, touchX, touchY;
    private State state = State.READY;
    private boolean shield;

    private enum State { READY, RUNNING, PAUSED, OVER }
    private static final class Thing {
        final ModelInstance model;
        final int lane;
        final boolean coin;
        final boolean slideHazard;
        float z;
        Thing(Model model, int lane, float z, boolean coin, boolean slideHazard) {
            this.model = new ModelInstance(model); this.lane = lane; this.z = z;
            this.coin = coin; this.slideHazard = slideHazard;
        }
    }

    @Override public void create() {
        modelBatch = new ModelBatch(); hudBatch = new SpriteBatch(); font = new BitmapFont();
        font.setColor(Color.WHITE);
        camera = new PerspectiveCamera(65, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 5.2f, 8.5f); camera.lookAt(0, 1.4f, -8); camera.near = .1f; camera.far = 120;
        hudCamera = new OrthographicCamera(); hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .72f, .76f, .86f, 1f));
        environment.add(new DirectionalLight().set(.9f, .86f, .75f, -1, -1, -1));
        ModelBuilder mb = new ModelBuilder();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        floorModel = mb.createBox(2.12f, .35f, 10.2f, material(new Color(.15f, .23f, .34f, 1)), attr);
        playerModel = mb.createBox(1.05f, 1.35f, .85f, material(new Color(.12f, .82f, .98f, 1)), attr);
        crateModel = mb.createBox(1.18f, 1.25f, .95f, material(new Color(.95f, .34f, .18f, 1)), attr);
        beamModel = mb.createBox(1.8f, .35f, .7f, material(new Color(.98f, .72f, .18f, 1)), attr);
        coinModel = mb.createBox(.48f, .48f, .12f, material(new Color(1f, .83f, .18f, 1)), attr);
        playerInstance = new ModelInstance(playerModel);
        for (int i = 0; i < 12; i++) for (int l = 0; l < 3; l++) {
            ModelInstance tile = new ModelInstance(floorModel);
            tile.transform.setToTranslation(LANES[l], -.22f, -i * 10f);
            track.add(tile);
        }
        best = Gdx.app.getPreferences("block-rush").getInteger("best", 0);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) move(-1);
                else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) move(1);
                else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.UP || keycode == Input.Keys.W) jump();
                else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) slide();
                else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) togglePause();
                return true;
            }
            @Override public boolean touchDown(int x, int y, int pointer, int button) { touchX=x; touchY=y; return true; }
            @Override public boolean touchUp(int x, int y, int pointer, int button) {
                float dx=x-touchX, dy=y-touchY;
                if (Math.max(Math.abs(dx),Math.abs(dy)) < 28) { if (state==State.READY) start(); else if(state==State.OVER) start(); else if(state==State.PAUSED) togglePause(); return true; }
                if (Math.abs(dx)>Math.abs(dy)) move(dx<0?-1:1); else if (dy<0) jump(); else slide();
                return true;
            }
        });
    }

    private Material material(Color c) { return new Material(ColorAttribute.createDiffuse(c)); }
    private void move(int direction) { if (state==State.READY) start(); if (state==State.RUNNING) lane=MathUtils.clamp(lane+direction,0,2); }
    private void jump() { if (state==State.READY) start(); if (state==State.RUNNING && jumpY<=0.001f && slideTimer<=0) jumpVelocity=8.5f; }
    private void slide() { if (state==State.READY) start(); if (state==State.RUNNING && jumpY<=.05f) slideTimer=.72f; }
    private void togglePause() { if(state==State.RUNNING) state=State.PAUSED; else if(state==State.PAUSED) state=State.RUNNING; }
    private void start() { things.clear(); lane=1; playerX=0; jumpY=0; jumpVelocity=0; slideTimer=0; elapsed=0; distance=0; coins=0; speed=12; spawnTimer=.7f; trackOffset=0; state=State.RUNNING; }

    @Override public void render() {
        float dt=Math.min(Gdx.graphics.getDeltaTime(), .033f);
        if(state==State.RUNNING) update(dt);
        ScreenUtils.clear(.055f,.09f,.16f,1f,true);
        camera.viewportWidth=Gdx.graphics.getWidth(); camera.viewportHeight=Gdx.graphics.getHeight(); camera.update();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        modelBatch.begin(camera);
        for(ModelInstance tile:track) modelBatch.render(tile,environment);
        for(Thing t:things) { t.model.transform.setToTranslation(LANES[t.lane], t.coin?1.05f:.65f, t.z); modelBatch.render(t.model,environment); }
        playerInstance.transform.setToTranslation(playerX, .75f+jumpY, 0);
        if(slideTimer>0) playerInstance.transform.scale(1.18f,.48f,1.18f);
        modelBatch.render(playerInstance,environment);
        modelBatch.end();
        drawHud();
    }

    private void update(float dt) {
        elapsed+=dt; speed=Math.min(24f,12f+elapsed*.13f); distance+=speed*dt; trackOffset+=speed*dt;
        playerX=MathUtils.lerp(playerX,LANES[lane],Math.min(1,dt*13));
        if(jumpVelocity!=0||jumpY>0){ jumpY+=jumpVelocity*dt; jumpVelocity-=22f*dt; if(jumpY<0){jumpY=0;jumpVelocity=0;} }
        slideTimer=Math.max(0,slideTimer-dt);
        for(ModelInstance tile:track){ tile.transform.getTranslation(TMP); float z=TMP.z+speed*dt; if(z>8)z-=120; tile.transform.setToTranslation(TMP.x,-.22f,z); }
        spawnTimer-=dt;
        if(spawnTimer<=0){ spawnPattern(); spawnTimer=Math.max(.62f,1.15f-elapsed*.004f)+MathUtils.random(.05f,.38f); }
        for(int i=things.size-1;i>=0;i--){
            Thing t=things.get(i); t.z+=speed*dt;
            if(Math.abs(t.z)<.72f && t.lane==lane){
                if(t.coin){coins++;things.removeIndex(i);continue;}
                if(t.slideHazard?slideTimer<=0:jumpY<.72f){ if(shield){shield=false;things.removeIndex(i);}else{gameOver();} }
            }
            if(t.z>8) things.removeIndex(i);
        }
    }
    private static final Vector3 TMP=new Vector3();
    private void spawnPattern(){
        int safe=MathUtils.random(0,2);
        for(int l=0;l<3;l++) if(l!=safe && MathUtils.randomBoolean(.72f)) {
            boolean overhead = MathUtils.randomBoolean(.28f);
            things.add(new Thing(overhead?beamModel:crateModel,l,-58,false,overhead));
        }
        // A coin trail marks the intentionally open lane.
        for(int i=0;i<4;i++) things.add(new Thing(coinModel,safe,-46-i*2.4f,true,false));
    }
    private void gameOver(){state=State.OVER;int score=(int)distance;if(score>best){best=score;Gdx.app.getPreferences("block-rush").putInteger("best",best).flush();}}
    private void drawHud(){
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        font.getData().setScale(Math.max(1f,Gdx.graphics.getWidth()/430f));
        font.draw(hudBatch,"BLOCK RUSH",24,Gdx.graphics.getHeight()-28);
        font.draw(hudBatch,"DIST  "+(int)distance+"     COINS  "+coins,Gdx.graphics.getWidth()*.42f,Gdx.graphics.getHeight()-28);
        font.draw(hudBatch,"BEST  "+best,Gdx.graphics.getWidth()-125,Gdx.graphics.getHeight()-58);
        String msg=state==State.READY?"TAP OR PRESS A KEY TO RUN":state==State.PAUSED?"PAUSED - TAP / P TO RESUME":state==State.OVER?"RUN OVER   "+(int)distance+" m\nTAP TO RESTART":"LEFT / RIGHT     UP JUMP     DOWN SLIDE";
        if(state!=State.RUNNING) { font.getData().setScale(Math.max(1.2f,Gdx.graphics.getWidth()/330f)); font.draw(hudBatch,msg,Gdx.graphics.getWidth()*.12f,Gdx.graphics.getHeight()*.53f); }
        hudBatch.end();
    }
    @Override public void resize(int width,int height){ if(camera!=null){camera.viewportWidth=width;camera.viewportHeight=height;camera.update();} if(hudCamera!=null){hudCamera.setToOrtho(false,width,height);hudCamera.update();} }
    @Override public void dispose(){ if(modelBatch!=null)modelBatch.dispose();if(hudBatch!=null)hudBatch.dispose();if(font!=null)font.dispose();if(floorModel!=null)floorModel.dispose();if(playerModel!=null)playerModel.dispose();if(crateModel!=null)crateModel.dispose();if(beamModel!=null)beamModel.dispose();if(coinModel!=null)coinModel.dispose(); }
}
