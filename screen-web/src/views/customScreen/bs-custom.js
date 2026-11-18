export function addModule(ev, moduleItemW, moduleItemH, currentModule, moduleListView, rightModulePos){
    let idx = findNearest (ev,moduleItemW,moduleItemH,rightModulePos);
    if(idx === false){
        moduleListView.pop();
        return { msg: '超出预览范围，请重新拖拽', moduleListView };
    }
    let items = moduleListView.filter(opt => opt.name == currentModule.name);
    if(items.length > 1 && !currentModule.repeatFlag){
        moduleListView.pop();
        console.log(moduleListView,'moduleListView');
        return { msg: '组件重复，请重新拖拽', moduleListView };
    }
    let {top, left} = rightModulePos[idx];
    let {x,y} = moduleListView[moduleListView.length-1];
    let h = top + (y - 1) * moduleItemH;
    if(h > moduleItemH * 3){
        moduleListView.pop();
        return { msg: '当前位置不够，请重新选择', moduleListView };
    }
    let w = left + (x - 1) * moduleItemW;
    if(w > moduleItemW * 4){
        moduleListView.pop();
        return { msg: '当前位置不够，请重新选择', moduleListView };
    }
    let seatPos = [];
    for(let i = 0; i < y; i++){
        seatPos.push(idx + 4*i)
    }
    if(x == 1 || y == 1){
        for(let i = 0; i < x; i++){
            seatPos.push(idx + i)
        }
    }else{
        for(let i = 0; i < x; i++){
            seatPos.push(seatPos[i]+1)
        }
    }
    seatPos = Array.from(new Set(seatPos));

    let flagSeat = false;
    for(let i = 0; i < seatPos.length; i++){
        let _i = seatPos[i];
        if(rightModulePos[_i]['flag']){
            flagSeat = true
            break;
        }else{
            rightModulePos[_i]['flag'] = true;
        }
    }
    if(flagSeat){
        moduleListView.pop();
        return { msg: '当前位置已有组件，请重新选择', moduleListView };
    }

    //图例--项目桥梁列表[3,7,11]  各桥梁架设情况[9,10]
    let legendR = [];
    if(currentModule.moduleId === '10007'){
        legendR = [3,7,11];
    }else if(currentModule.moduleId === '10021'){
        legendR = [9,10];
    }
    legendR.forEach(o => {
        rightModulePos[o]['flag'] = true;
    })
    return {
        top,
        left,
        idx,
        seatPos,
        rightModulePos
    }
}

export function delModule(data, moduleListView,moduleList,rightModulePos){
    let moduleId = data.moduleId;
    let idx = moduleListView.findIndex(opt => opt.moduleId == moduleId);
    let module = moduleList.find(opt => opt.moduleId == moduleId);
    moduleListView.splice(idx,1);
    module['flag'] = false;
    let arrIdx = data.seatPos.split(',');
    arrIdx.forEach(i => {
        rightModulePos[i]['flag'] = false;
    })

    //图例--项目桥梁列表[3,7,11]  各桥梁架设情况[9,10]
    let legendR = [];
    if(data.moduleId === '10007'){
        legendR = [3,7,11];
    }else if(data.moduleId === '10021'){
        legendR = [9,10];
    }
    legendR.forEach(o => {
        rightModulePos[o]['flag'] = false;
    })

    return {
        moduleListView,
        rightModulePos
    }
}

export function findNearest (ev,moduleItemW,moduleItemH,rightModulePos){
    let x = ev.originalEvent.layerX;
    let y = ev.originalEvent.layerY;
    if(x > moduleItemW*4 || y > moduleItemH*3){
        return false;
    }

    let iMin = 999999;
    let iMinIndex = -1;
    rightModulePos.forEach((opt,idx) => {
        let a = opt.top - y;
        let b = opt.left - x;
        let dis = Math.sqrt(a*a+b*b);
        if(dis < iMin){
            iMin = dis;
            iMinIndex = idx
        }
    })

    if(iMinIndex == -1){
        return false
    }else{
        return iMinIndex;
    }
}
