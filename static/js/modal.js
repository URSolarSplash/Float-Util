//////////////////////////////////////////////////////////
// Modal Dialog manager
// Handles a simple modal for error and status messages.
// Requires jQuery.
//////////////////////////////////////////////////////////

// On initialization, make sure the modal is hidden.
$(function(){
    hideModal();
});

// Shows a modal with the provided title and message.
function showModal(title,message){
    $("#modal-title").text(title);
    var footer = `<br><br>
    <hr>
    <div id="closeModal" class="button">Close</div>
    `;
    $("#modal-body").html(message+footer);
    $("#modal-overlay").show();
    $("#closeModal").click(function(){
        hideModal();
    });
}

// Hides the modal, if it's visible.
function hideModal(){
    $("#modal-overlay").hide();
}

//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
