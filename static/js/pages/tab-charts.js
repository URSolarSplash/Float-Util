var stabilityChartTop, stabilityChartBottom;

Chart.defaults.global.defaultFontFamily = '"UbuntuMono",sans-serif';

$(function(){
    data = []
    stabilityChartTop = new Chart(document.getElementById("stabilityChartTop").getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                data: [],
                label: "Stability",
                borderColor: "#ffff00",
                fill: false,
                pointRadius: 0,
                lineTension: 0,
                borderWidth:2,
                yAxisID: "y-axis-left"
            }]
        },
        options: {
            title: {
                display: true,
                text: 'Primary Stability (X Axis)'
            },
            maintainAspectRatio: false,
            scales: {
                xAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Roll Angle (deg)',
                        fontFamily: "Ubuntu"
                    },
                }],
                yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Righting Moment (Nm)',
                        fontFamily: "Ubuntu"
                    }
                }]
            },
            tooltips: {enabled: false},
            hover: {mode: null},
            legend: {
                display: false
            }
        }
    });
    stabilityChartBottom = new Chart(document.getElementById("stabilityChartBottom").getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                data: [],
                label: "Stability",
                borderColor: "#ffff00",
                fill: false,
                pointRadius: 0,
                lineTension: 0,
                borderWidth:2,
                yAxisID: "y-axis-left"
            }]
        },
        options: {
            title: {
                display: true,
                text: 'Secondary Stability (Z Axis)'
            },
            maintainAspectRatio: false,
            scales: {
                xAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Pitch Angle (deg)',
                        fontFamily: "Ubuntu"
                    },
                }],
                yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Righting Moment (Nm)',
                        fontFamily: "Ubuntu"
                    }
                }]
            },
            legend: {
                display: false
            }
        }
    });
})


function updateCharts(){

}
