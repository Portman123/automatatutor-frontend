var Editor = {
  curConfigDfa: {
    dimensions: [740,480]
  },
  curConfigMin: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasDfa) {
      Editor.canvasDfa = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfigDfa, 'detaut');
  }
  if(!Editor.canvasMin) {
      Editor.canvasMin = new $.SvgCanvas("#svgcanvasmin", Editor.curConfigMin, 'detaut');
  }
}

$(document).ready(function() {
  initCanvas();
}); 


