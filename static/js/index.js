//////////////////////////////////////////////////////////
// Main html page JS
// Handles all page mechanics.
//////////////////////////////////////////////////////////

// Create an instance of the application state
var Application = new ApplicationState();

logger.log("[Main Thread] Application state object initialized.");

// On page initialization
$(function(){

    // Set up display options dialog
    $("#displayOptions").click(function(){
        showModal("Display Options",`
        <input type="checkbox" id="display-water" name="display-water" value="display-water">
        <label for="display-water">Display Water Surface</label><br>
        <input type="checkbox" id="wireframe" name="wireframe" value="wireframe">
        <label for="wireframe">Wireframe</label><br>
        <input type="checkbox" id="debug" name="debug" value="debug">
        <label for="debug">Debug Mode</label><br>

        <br>
        `);
        $('#display-water').prop('checked',Application.showWater);
        $('#wireframe').prop('checked',Application.wireframe);
        $('#debug').prop('checked',Application.debug);

        $("#display-water").change(function(){
            Application.showWater = $(this).is(':checked');
            updateDisplayState();
        });
        $("#wireframe").change(function(){
            Application.wireframe = $(this).is(':checked');
            updateDisplayState();
        });
        $("#debug").change(function(){
            Application.debug = $(this).is(':checked');
            updateDisplayState();
        });
    });

    $("#update-panel").click(function(){
        Application.update();
    });

    // Set up sidebar panel toggling
    $(".status-bar-toggle").text("hide");
    $(".status-bar-toggle").click(function(){
        if ($(this).text() == "hide"){
            $(this).parent().addClass("status-pane-hidden");
            $(this).text("show");
        } else {
            $(this).parent().removeClass("status-pane-hidden");
            $(this).text("hide");
        }
    });
});

//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
