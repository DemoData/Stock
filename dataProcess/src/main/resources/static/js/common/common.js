$.fn.dialog.defaults.closed = true;// 修改默认初始化会话窗口是关闭的
$.fn.dialog.defaults.modal = true;// 修改会话窗口都是模式化的窗口

// 自己扩展一个序列化表单参数为json对象的方法
$.fn.serializeJSON = function() {
	// 把查询表单中的数据封装成json数组
	var array = this.serializeArray();
	var paramObj = {};
	// 通过循环遍历数组，把这个变成一个json对象
	for ( var i = 0; i < array.length; i++) {
		paramObj[array[i].name] = array[i].value;
	}
	return paramObj;
};
//一个通用的关联关系的显示名称的格式化方法
function objectFormatter(value, row, index) {
	// 这里||就表示前一个没有就找后一个
	return value ? value.nickname || value.name || value.title || "": "<font color='red'>暂无</font>";
}
//状态格式化
function statusFormatter(value,row,index){
	return value == 0 ? "<font color='green'>正常</font>" : "<font color='red'>废除</font>";
}
function genderFormatter(value,row,index){
	switch (value) {
	case 0:
		return '女';
		break;
	case 1:
		return '男';
		break;
	default:
		return '保密';
		break;
	}
}
//用于部门显示孩子节点的格式化方法
function childrenFormatter(value, row, index) {
	if (value && value instanceof Array) {
		var result = "";
		for ( var i = 0; i < value.length; i++) {
			result += value[i] ? value[i].name+"," : "";
		}
		result = result ? result.substring(0,result.length-1) : "<font color='red'>暂无</font>";
		return result;
	}
}
//扩充验证方法
$.extend($.fn.validatebox.defaults.rules, {    
    minLength: {    
        validator: function(value, param){    
            return value.length >= param[0];    
        },
        message: '请输入最少{0}个字符'   
    },
    equals: {    
        validator: function(value,param){    
            return value == $(param[0]).val();    
        },    
        message: '两次密码输入不一致'   
    } 
}); 
//实现radio控件的非空的验证
$.extend($.fn.validatebox.defaults.rules, {
	//对radio验证
    radio: {
        validator: function (value, param) {
            var frm = param[0], groupname = param[1], ok = false;
            $('input[name="' + groupname + '"]', document[frm]).each(function () { //查找表单中所有此名称的radio
                if (this.checked) { ok = true; return false; }
            });
            return ok;
        },
        message: '需要选择性别!'
    }
});