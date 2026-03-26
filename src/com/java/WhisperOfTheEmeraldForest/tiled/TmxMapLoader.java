package com.java.WhisperOfTheEmeraldForest.tiled;

import com.java.WhisperOfTheEmeraldForest.util.Assets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TmxMapLoader {
    public TiledMap load(String relativePath) {
        try {
            Path mapPath = Paths.get(System.getProperty("user.dir"), "assets", relativePath).normalize();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mapPath.toFile());
            doc.getDocumentElement().normalize();

            Element mapElement = doc.getDocumentElement();
            int width = Integer.parseInt(mapElement.getAttribute("width"));
            int height = Integer.parseInt(mapElement.getAttribute("height"));
            int tileWidth = Integer.parseInt(mapElement.getAttribute("tilewidth"));
            int tileHeight = Integer.parseInt(mapElement.getAttribute("tileheight"));

            List<Tileset> tilesets = new ArrayList<>();
            NodeList tilesetNodes = mapElement.getElementsByTagName("tileset");
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                Node node = tilesetNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element tilesetElement = (Element) node;
                int firstGid = Integer.parseInt(tilesetElement.getAttribute("firstgid"));
                String source = tilesetElement.getAttribute("source");
                if (source == null || source.isEmpty()) {
                    continue;
                }
                Path tsxPath = mapPath.getParent().resolve(source).normalize();
                Tileset tileset = loadTileset(firstGid, tsxPath.toFile());
                tilesets.add(tileset);
            }

            List<TiledLayer> layers = new ArrayList<>();
            NodeList layerNodes = mapElement.getElementsByTagName("layer");
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Node node = layerNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element layerElement = (Element) node;
                String name = layerElement.getAttribute("name");
                int layerWidth = Integer.parseInt(layerElement.getAttribute("width"));
                int layerHeight = Integer.parseInt(layerElement.getAttribute("height"));
                Element dataElement = (Element) layerElement.getElementsByTagName("data").item(0);
                String encoding = dataElement.getAttribute("encoding");
                if (!"csv".equalsIgnoreCase(encoding)) {
                    throw new IllegalStateException("Unsupported TMX encoding: " + encoding);
                }
                String csv = dataElement.getTextContent().trim();
                String[] values = csv.split("[,\\s]+");
                if (values.length < layerWidth * layerHeight) {
                    throw new IllegalStateException("Invalid TMX data length for layer " + name);
                }
                int[] gids = new int[layerWidth * layerHeight];
                int index = 0;
                for (int row = 0; row < layerHeight; row++) {
                    int targetRow = layerHeight - 1 - row;
                    for (int col = 0; col < layerWidth; col++) {
                        long rawGid = Long.parseLong(values[index++].trim());
                        int gidWithFlags = (int) rawGid;
                        gids[targetRow * layerWidth + col] = gidWithFlags;
                    }
                }
                layers.add(new TiledLayer(name, layerWidth, layerHeight, gids));
            }

            return new TiledMap(width, height, tileWidth, tileHeight, layers, tilesets);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load TMX: " + relativePath, e);
        }
    }

    private Tileset loadTileset(int firstGid, File tsxFile) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tsxFile);
        doc.getDocumentElement().normalize();

        Element tilesetElement = doc.getDocumentElement();
        int tileWidth = Integer.parseInt(tilesetElement.getAttribute("tilewidth"));
        int tileHeight = Integer.parseInt(tilesetElement.getAttribute("tileheight"));
        int tileCount = Integer.parseInt(tilesetElement.getAttribute("tilecount"));
        int columns = Integer.parseInt(tilesetElement.getAttribute("columns"));
        int margin = tilesetElement.hasAttribute("margin") ? Integer.parseInt(tilesetElement.getAttribute("margin")) : 0;
        int spacing = tilesetElement.hasAttribute("spacing") ? Integer.parseInt(tilesetElement.getAttribute("spacing")) : 0;

        Element imageElement = (Element) tilesetElement.getElementsByTagName("image").item(0);
        String source = imageElement.getAttribute("source");
        Path imagePath = tsxFile.toPath().getParent().resolve(source).normalize();
        Path assetsRoot = Paths.get(System.getProperty("user.dir"), "assets");
        Path relative = assetsRoot.relativize(imagePath);
        BufferedImage image = Assets.load(relative.toString().replace("\\\\", "/"));

        return new Tileset(firstGid, tileWidth, tileHeight, columns, tileCount, margin, spacing, image);
    }
}
