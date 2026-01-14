package your.package.maps;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class TiledGameMap {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    public TiledGameMap() {
        map = new TmxMapLoader().load("maps/map-1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
    }

    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
