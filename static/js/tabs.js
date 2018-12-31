//////////////////////////////////////////////////////////
// Tab manager
// Handles a simple tab system for the application.
// Tabs are hidden and shown using CSS, but all tabs are
// always loaded.
// Requires jQuery.
//////////////////////////////////////////////////////////

var currentTab = "";

// On page load, set up the tab system
$(function(){
    // Set the current tab to default
    setTab("model");

    // Set a click listener for all tab objects
    $(".tab").click(function(){
        // Trim the -tab from the name and call setTab
        tabId = this.id.substring(0, this.id.length - 4);
        setTab(tabId);
    })
});

// Changes tabs to the tab specified.
function setTab(tab){
    if (tab == currentTab){
        return;
    } else {
        currentTab = tab;
    }

    // Set all the tabs to inactive and set the active tab
    $(".tab").removeClass("tab-active").addClass("tab-inactive");
    $("#"+tab+"-tab").removeClass("tab-inactive").addClass("tab-active");

    // Hide all the tab panes, show active tab pane
    $(".tab-pane").hide();
    $("#"+tab+"-tab-pane").show();
}

//////////////////////////////////////////////////////////
// End of File
//////////////////////////////////////////////////////////
