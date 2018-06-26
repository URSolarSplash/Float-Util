package Display;


import org.lwjgl.nanovg.NVGColor;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class RenderWindow {

    public long window;
    private long nanoVG;
    private NVGColor nanoVgColor;
    private int nanoVgFont;
    public int WIDTH = 1280;
    public int HEIGHT = 720;
    private double mouseX = 0;
    private double mouseY = 0;

    public RenderWindow(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }

    public boolean shouldAbort(){
        return glfwWindowShouldClose(window);
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure window
        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        //glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        System.out.println(glfwGetVersionString());


        window = glfwCreateWindow(WIDTH, HEIGHT, "URSS Telemetry V3", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Callback to handle key presses
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true);
            }
        });

        centerWindow();

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        //Init graphics
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_STENCIL_TEST);

        //Init app stuff
        nanoVG = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        nanoVgColor = NVGColor.create();
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        nanoVgFont = nvgCreateFont(nanoVG,"default","resources/F25_Bank_Printer.ttf");
        nanoVgFont = nvgCreateFont(nanoVG,"monospace","resources/Courier New Bold.ttf");

        instructions = new ArrayList<>();
        instructions.add("ESC = Exit");

        System.out.println("Finished initializing.");
    }

    public void startFrame(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);


    }

    public void endFrame(){

        //Sync to screen
        glfwSwapBuffers(window);
        glfwPollEvents();
        updateMousePos();
        hasContext = false;
    }

    public void terminate(){

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public boolean hasContext;
    public ArrayList<String> instructions;

    public void centerWindow() {
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
    }

    public void restoreState(){
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void updateMousePos(){
        //Get mouse position
        DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, posX, posY);
        IntBuffer w = BufferUtils.createIntBuffer(4);
        IntBuffer h = BufferUtils.createIntBuffer(4);
        glfwGetWindowSize(window, w, h);
        int width = w.get(0);
        int height = h.get(0);
        mouseX = (posX.get(0) / width)*WIDTH;
        mouseY = (posY.get(0) / height)*HEIGHT;

    }

    public void drawLine(double x1, double y1, double x2, double y2){
        drawLine(x1,y1,x2,y2,new RGBA(255,255,255,255));
    }
    public void drawLine(double x1, double y1, double x2, double y2, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);
        nvgMoveTo(nanoVG,(float)x1,(float)y1);
        nvgLineTo(nanoVG,(float)x2,(float)y2);
        nvgStrokeColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgStrokeWidth(nanoVG,1);
        nvgLineCap(nanoVG,NVG_BUTT);
        nvgStroke(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }
    ArrayList<Point> tempGraphLines = new ArrayList<Point>();


    double distance (Point p1, Point p2) {
        Point result = new Point(0,0);
        result.y = Math.abs (p1.y - p2.y);
        result.x = Math.abs (p1.x- p2.x);
        return Math.sqrt((result.y)*(result.y) +(result.x)*(result.x));
    }

    double normalize(double value, double min, double max) {
        //convert a value in a range to a value in 0->1
        return (value - min) / (max - min);
    }

    public RGBA red = new RGBA(255, 0, 0, 255);
    public RGBA white = new RGBA(255,255,255,255);
    public RGBA black = new RGBA(0,0,0,255);
    public RGBA rochesterBlue = new RGBA(0,59,113,255);
    public RGBA rochesterYellow = new RGBA(255,209,0,255);
    public RGBA cyan = new RGBA(0,255,255,255);

    public void drawBox(double x1, double y1, double x2, double y2, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);
        nvgRect(nanoVG,(float)x1,(float)y1,(float)x2-(float)x1,(float)y2-(float)y1);
        nvgFillColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgFill(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }

    public void drawCircle(double x1, double y1, double radius, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);
        nvgCircle(nanoVG,(float)x1,(float)y1,(float)radius);
        nvgFillColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgFill(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }

    public void drawOutlineBox(double x1, double y1, double x2, double y2, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);
        nvgRect(nanoVG,(float)x1,(float)y1,(float)x2-(float)x1,(float)y2-(float)y1);
        nvgStrokeColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgStrokeWidth(nanoVG,2);
        nvgStroke(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }

    public void drawFilledLines(ArrayList<Point> points, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);

        boolean firstPoint = true;
        for (Point point : points) {
            if (firstPoint){
                nvgMoveTo(nanoVG,(float)point.x,(float)point.y);
                firstPoint = false;
            } else {
                nvgLineTo(nanoVG,(float)point.x,(float)point.y);
            }
        }
        nvgFillColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));

        nvgLineCap(nanoVG,NVG_ROUND);
        nvgFill(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }

    public void drawLines(ArrayList<Point> points, RGBA color){

        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,1);
        nvgBeginPath(nanoVG);

        boolean firstPoint = true;
        for (Point point : points) {
            if (firstPoint){
                nvgMoveTo(nanoVG,(float)point.x,(float)point.y);
                firstPoint = false;
            } else {
                nvgLineTo(nanoVG,(float)point.x,(float)point.y);
            }
        }
        nvgStrokeColor(nanoVG, nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgStrokeWidth(nanoVG,1);
        nvgLineCap(nanoVG,NVG_ROUND);
        nvgStroke(nanoVG);
        nvgEndFrame(nanoVG);
        restoreState();
    }

    public Point pixelsToScreenSpace(Point in){
        return pixelsToScreenSpace(in.x,in.y);
    }
    public Point pixelsToScreenSpace(double x, double y){
        Point out = new Point((x/(WIDTH * 0.5f)) -1f,((y/(HEIGHT * 0.5f)) - 1f)*-1f);
        return out;
    }

    String font = "default";
    int size = 18;

    public void setFont(String font, int size){
        this.font = font;
        this.size = size;
    }

    public void drawText(double x, double y, String text){
        drawText(x,y,text,new RGBA(255,255,255,255));
    }

    public void drawText(double x, double y, String text,RGBA color){
        nvgBeginFrame(nanoVG,WIDTH,HEIGHT,2);
        nvgFontFace(nanoVG,font);
        nvgTextAlign(nanoVG, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFontSize(nanoVG,size);
        nvgFillColor(nanoVG,nvgRGBA((byte)color.r,(byte)color.g,(byte)color.b,(byte)color.a, nanoVgColor));
        nvgText(nanoVG,(float)x,(float)y,text);
        nvgEndFrame(nanoVG);
        restoreState();
    }
}
