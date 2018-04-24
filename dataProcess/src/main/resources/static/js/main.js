//页面加载完毕后运行
$(function() {
	$("#menuTree").tree({
		url : "/systemMenu_getTreeData.action",
		method : "get",
		animate : true,
		onClick:function(node){
			var centerTabs = $("#centerTabs");
			var text = node.text;
			var attr = node.attributes;
			if(!attr){
				return;
			}
			var url = attr.url;
			if(!url){//如果url没有值
				return;
			}
			if(centerTabs.tabs("exists",text)){//当前面板已存在
				centerTabs.tabs("select",text);//选中当前面板
			}else{
				centerTabs.tabs("add",{
					title:text,
					closable:true,
					content:'<iframe frameborder="0" width="100%" height="100%" src="'+url+'"></iframe>'
				});
			}
		}
	});
	//声明需要的变量
	var editPwdBtn,logoutBtn,passwordDlg,passwordForm;
	editPwdBtn = $("#editPwdBtn");
	logoutBtn = $("#logoutBtn");
	passwordDlg = $("#passwordDlg");
	passwordForm = $("#passwordForm");
	//初始化修改密码框
	passwordDlg.dialog({
		title:"修改密码",
		width:400,
		height:200,
		iconCls:'icon-save',
		resizable:true,
		modal:true,
		closed:true,
		buttons:[{
			text:"提交",
			iconCls:"icon-ok",
			handler:submitPwd
		},{
			text:"取消",
			iconCls:"icon-cancel",
			handler:function(){
				passwordDlg.dialog("close");
			}
		}]
	});
	editPwdBtn.click(function(){
		passwordForm.form("clear");
		passwordDlg.dialog("open");
		passwordDlg.dialog("center");
	});
	logoutBtn.click(function(){
		$.messager.confirm('确认','确认注销？',function(r){    
		    if (r){    
				$.get("/login_logout.action", function(data){
					$.messager.alert("提示",data.msg,"info",function(){
							//注销成功就跳转到登录界面
							location.href = "/login.action";
						});
					});
		    }    
		}); 
	});
	function submitPwd(){
		passwordForm.form("submit",{
				url:"/login_editPassword.action",
				//表单提交成功后触发 ajax异步给后台发送请求验证
				success:function(data){
					data = $.parseJSON(data);
					if(data.success){
						$.messager.alert("提示",data.msg,"info",function(){
							passwordDlg.dialog("close");
						});
					}else{
						$.messager.alert("提示",data.msg,"info",function(){
							if(data.errorCode == 3001){
								$("#oPwd").focus();
							}else if(data.errorCode == 3002){
								$("#rPwd").focus();
							}else if(data.errorCode == 3003){
								$("#nPwd").focus();
							}
						});
					}
				}
			});
		}
});
//一个用于修改主题样式的方法
//通过console.debug(arguments);可以看到传入的参数有newValue,oldValue两个，这个是eazyui放入的
function onChangeTheme(newThemeValue,oldThemeValue){
	if(newThemeValue){
		//修改主页的主题样式
		$("#themeLink").attr("href","/jquery-easyui-1.5.4/themes/"+newThemeValue+"/easyui.css");
		//修改已近打开的子页面的主题样式
		//由于子页面是一个iframe打开的，所以要先得到iframe然后在得到iframe里面的ducument,
		//在查找里面的themeLink然后修改
		//console.debug($("iframe"));
		$("iframe").each(function(index,iframe){
			$(iframe.contentDocument).find("#themeLink").
				attr("href","/jquery-easyui-1.5.4/themes/"+newThemeValue+"/easyui.css");;
		});
		//这个属性用于给打开新的子页面时动态从父窗体获取主题样式
		//给window上面增加一个config属性
		//这相当于是一个全局的变量
		window.config = {
				themeValue : newThemeValue
		};
	}
}
