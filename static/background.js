logger.log('[Background Thread] Background thread initialized.');

var isProcessing = false;

$(function(){

    ipcRenderer.on('simulation-start', (startTime) => {

	});
    ipcRenderer.on('simulation-cancel', (startTime) => {

	});



});

function process




function sendProgressPacket(){
    ipcRenderer.send('background-response', {
        result: task(),
        startTime: startTime
    });
}

function sendSuccessPacket(){

}

function sendErrorPacket(){

}
