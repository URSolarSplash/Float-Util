var container, scene, camera, controls, renderer;
var water, sky, sunSphere, sunLight;
var model,grid,waterModel;
var viewDistance = 2000;
var stabilityAxisMarker, axesHelper;
var waterMaterial = new THREE.MeshPhongMaterial( { wireframe: Application.wireframe, transparent: true, opacity: 0.5, color: 0x0000ff, specular: 0x111111, shininess: 200} );
var material = new THREE.MeshPhongMaterial( { wireframe: Application.wireframe, color: 0xff5533, specular: 0x111111, shininess: 200, side: THREE.DoubleSide} );
var position = {x:0, y:0, z:0};
var axes = [];
var axesContainer,axesScene,axesCamera,axesRenderer;

function setupAxes(){
    axesContainer = document.getElementById('model-axes');
    axesScene = new THREE.Scene();
    var range = 0.6;
	axesCamera = new THREE.OrthographicCamera(-range,range,range,-range,0.01,100);
	axesRenderer = new THREE.WebGLRenderer({antialias: false, alpha: true});
	axesRenderer.setSize($(axesContainer).width(),$(axesContainer).height(),false);
	axesContainer.appendChild(axesRenderer.domElement);
    for (var x = 0; x < 3; x++){
        for (var y = 0; y < 3; y++){
            for (var z = 0; z < 3; z++){
                var axe = new THREE.AxesHelper(0.5);
                var offset = 0.02;
                axe.position.set(x*offset - 0.2,y*offset - 0.2,z*offset - 0.2);
                axes.push(axe);
                axesScene.add(axe);
            }
        }
    }
}

$(function(){
    container = document.getElementById('model-view-canvas');

    setupAxes();
    createScene();
    initSky();


    var stabilityAxisTexture = THREE.ImageUtils.loadTexture('static/img/stability-axis.png');
    stabilityAxisMarker = new THREE.Mesh(new THREE.PlaneGeometry(2, 1), new THREE.MeshBasicMaterial({map: stabilityAxisTexture, transparent: true, side: THREE.DoubleSide}));
    stabilityAxisMarker.position.set(0,0.5,0);
    scene.add(stabilityAxisMarker);

    axesHelper = new THREE.AxesHelper( 5 );

    controls.update();
    setModel(null);

    updateDisplayState();

    $("#model-zoom-in").click(function(){

    });
    $("#model-zoom-out").click(function(){

    });
    $("#model-axes").click(function(){
        // Reset camera to default view position
        resetCamera();
    });

	animate();
});

function resetCamera(){
    // Set camera rotation and position to a nice position I chose
    camera.position.set(3.321969174566935,1.9924758415577541,3.321969174566935);

    // Set camera position to fit model size
    camera.position.normalize();
    camera.position.multiplyScalar((controls.minDistance + controls.maxDistance)/2);
    controls.update();
}

function updateDisplayState(){
    if (Application.displayWater){
        scene.add(water);
    } else {
        scene.remove(water);
    }
    if (Application.displayWireframe){
        material.wireframe = true;
        waterMaterial.wireframe = true;
    } else {
        material.wireframe = false;
        waterMaterial.wireframe = false;
    }
    if (Application.displayDebug){
        scene.add(axesHelper);
        //scene.add(waterModel);
    } else {
        scene.remove(axesHelper);
        //scene.remove(waterModel);
    }
}

function setModel(modelGeometry){
    scene.remove(model);
    if (modelGeometry != null){
        model = new THREE.Mesh(modelGeometry, material);
        scene.add(model);
        model.position.set(position.x,position.y,position.z);

        var stabilityAxisSize = 0;
        if (Application.simulationStabilityAxis == 0){
            // X
            stabilityAxisSize = Application.sizeX*1.67;
            stabilityAxisMarker.rotation.set(0,0,0);
        } else {
            // Z
            stabilityAxisSize = Application.sizeZ*1.67;
            stabilityAxisMarker.rotation.set(0,Math.PI/2,0);
        }
        stabilityAxisMarker.scale.set(stabilityAxisSize,stabilityAxisSize,stabilityAxisSize);
        stabilityAxisMarker.position.set(0,0.5*stabilityAxisSize,0);

        controls.minDistance = Math.max(Application.maxSize,0.1);
        controls.maxDistance = Math.max(Application.maxSize*4,0.1);
        scene.fog.near = Math.max(Application.maxSize,0.1);
        scene.fog.far = viewDistance;
        controls.update();
    }
    //resetCamera();
    render();
}

function setModelPosition(pos){
    position.x = pos.x;
    position.y = pos.y;
    position.z = pos.z;
    model.position.set(position.x,position.y,position.z);
    render();
}

function createScene(){
    scene = new THREE.Scene();
	camera = new THREE.PerspectiveCamera( 75, $(container).width()/$(container).height(), 0.01,viewDistance );


	renderer = new THREE.WebGLRenderer({antialias: true});
	renderer.setSize($(container).width(),$(container).height(),false);
	container.appendChild(renderer.domElement);

    $(container).resize(function(){
        console.log("resized");
    });

    grid = new THREE.GridHelper(10, 10, 0xffffff, 0xffffff );
	//scene.add( grid );
    grid.position.set(0,-0.01,0);

	controls = new THREE.OrbitControls( camera, renderer.domElement );
	controls.maxPolarAngle = Math.PI / 2.2;
	controls.enableZoom = true;
	controls.enablePan = false;
    controls.minDistance = 1;
    controls.maxDistance = 10;

	var geometry = new THREE.BoxGeometry( 1, 1, 1 );
	var material = new THREE.MeshBasicMaterial( { color: 0x00ff00 } );
	model = new THREE.Mesh( geometry, material );
	scene.add( model );

	camera.position.z = 5;
}

function animate() {
	requestAnimationFrame(animate);
	render();
}

function render() {
	var time = performance.now() * 0.001;
	water.material.uniforms.time.value += 1.0 / 60.0;
	renderer.render( scene, camera );

    // Copy main camera angle and position to axes camera
    axesCamera.position.set(camera.position.x,camera.position.y,camera.position.z);
    axesCamera.lookAt(0,0,0);
    axesCamera.position.normalize();
    axesRenderer.render(axesScene,axesCamera);
}


$(window).resize(function(){
    camera.aspect = $(container).width()/$(container).height();
    camera.updateProjectionMatrix();
	renderer.setSize($(container).width(),$(container).height(),false);
});

function initSky() {
	// Add Sky
	sky = new THREE.Sky();
	sky.scale.setScalar( viewDistance);
	scene.add(sky);

    sunSphere = new THREE.Mesh(
					new THREE.SphereBufferGeometry( 20000, 16, 8 ),
					new THREE.MeshBasicMaterial( { color: 0xffffff } )
				);
	sunSphere.position.y = - 700000;
	sunSphere.visible = false;
	scene.add( sunSphere );

    fogColor = new THREE.Color(0xCAD7DB);
    scene.background = fogColor;
    scene.fog = new THREE.Fog(fogColor, 0.0025, 200);

    scene.add( new THREE.HemisphereLight(0xdddddd,0xdddddd));
    sunLight = new THREE.DirectionalLight( 0xffffff, 1 );
    sunLight.position.set(100,100,0);
    scene.add(sunLight);

	var effectController  = {
		turbidity: 5,
		rayleigh: 1,
		mieCoefficient: 0.005,
		mieDirectionalG: 0.8,
		luminance: 1,
		inclination: 0, // elevation / inclination
		azimuth: 0.15, // Facing front,
		sun: ! true
	};
	var distance = 400000;

	var uniforms = sky.material.uniforms;
	uniforms.turbidity.value = effectController.turbidity;
	uniforms.rayleigh.value = effectController.rayleigh;
	uniforms.luminance.value = effectController.luminance;
	uniforms.mieCoefficient.value = effectController.mieCoefficient;
	uniforms.mieDirectionalG.value = effectController.mieDirectionalG;
	var theta = Math.PI * ( effectController.inclination - 0.5 );
	var phi = 2 * Math.PI * ( effectController.azimuth - 0.5 );
	sunSphere.position.x = distance * Math.cos( phi );
	sunSphere.position.y = distance * Math.sin( phi ) * Math.sin( theta );
	sunSphere.position.z = distance * Math.sin( phi ) * Math.cos( theta );
    sunLight.position.set(sunSphere.position.x,sunSphere.position.y,sunSphere.position.z);
	sunSphere.visible = effectController.sun;
	uniforms.sunPosition.value.copy( sunSphere.position );

	var waterGeometry = new THREE.PlaneBufferGeometry( viewDistance, viewDistance );
    water = new THREE.Water(
		waterGeometry,
		{
			textureWidth: 512,
			textureHeight: 512,
			waterNormals: new THREE.TextureLoader().load( 'static/img/water-normals.jpg', function ( texture ) {
				texture.wrapS = texture.wrapT = THREE.RepeatWrapping;
			} ),
			alpha: 0.75,
			sunDirection: sunLight.position.clone().normalize(),
			sunColor: 0xffffff,
			waterColor: 0x001e0f,
			distortionScale: 0.2,
			fog: scene.fog !== undefined
		}
	);
    water.material.uniforms.size.value = 20;
	water.rotation.x = - Math.PI / 2;

	scene.add(water);
}
