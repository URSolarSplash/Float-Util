//////////////////////////////////////////////////////////
// Main html page JS
// Handles all page mechanics.
//////////////////////////////////////////////////////////

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
        <label for="debug">Debug Mode</label>
        `);
        $('#display-water').prop('checked',Application.displayWater);
        $('#wireframe').prop('checked',Application.displayWireframe);
        $('#debug').prop('checked',Application.displayDebug);

        $("#display-water").change(function(){
            Application.displayWater = $(this).is(':checked');
            updateDisplayState();
        });
        $("#wireframe").change(function(){
            Application.displayWireframe = $(this).is(':checked');
            updateDisplayState();
        });
        $("#debug").change(function(){
            Application.displayDebug = $(this).is(':checked');
            updateDisplayState();
        });
    });

    $("#model-properties").change(function(){
        console.log("Updating simulation preview...");
        Application.startUpdate(false);
    });

    setUpdateButtonReadyState();

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

function setUpdateButtonProgressState(progress){
    $("#update-panel").text("Updating, "+ progress+"%...");
    $("#update-panel").removeClass("update-panel-ready");
    $("#update-panel").off();
    /*
    $("#update-panel").click(function(){
        $("#update-panel").text("Cancelling...");
        Application.cancelUpdate();
    });*/
}

function setUpdateButtonReadyState(){
    $("#update-panel").text("Update Results");
    $("#update-panel").addClass("update-panel-ready");
    $("#update-panel").off();
    $("#update-panel").click(function(){
        setTimeout(function(){
            Application.startUpdate(true);
        },0);
    });
}

//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
