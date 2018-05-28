$(function () {
    var modleView = {
        progressBar: $("#progressBar"),
        submitBtn: $("#submitBtn"),
        clearBtn: $("#clearBtn"),
        submitForm: function () {
            var value = modleView.progressBar.progressbar('getValue'),
                batchNo = $("#prefix").val() + $("#batchNoDateBox").val(),
                timer;
            $('#patientForm').form('submit', {
                url: "/patient/processStart",
                onSubmit: function () {
                    var valid = $(this).form('enableValidation').form('validate');
                    if (valid) {
                        //禁用提交按钮
                        modleView.submitBtn.linkbutton('disable');
                        //显示进度条
                        modleView.progressBar.show();
                        timer = setInterval(function (e) {
                            $.ajax({
                                type: 'get',
                                dataType: 'json',
                                data: {"key": batchNo + "-patient"},
                                url: '/patient/getProgress',
                                success: function (count) {
                                    if (value < count) {
                                        value = count;
                                        modleView.progressBar.progressbar('setValue', value);
                                    }
                                    //由于多线程处理，剩下5%作为最后一批
                                    if (value >= 95) {
                                        clearInterval(timer);
                                    }
                                },
                                error: function () {
                                    clearInterval(timer);
                                }
                            });
                        }, 100);
                    }
                    return valid;
                },
                success: function (returnData) {
                    //启用提交按钮
                    modleView.submitBtn.linkbutton('enable');
                    clearInterval(timer);

                    modleView.progressBar.progressbar('setValue', 100);
                    $.messager.alert({
                        title: '提示',
                        msg: returnData ? returnData : "网络或系统错误！",
                        fn: function () {
                            modleView.progressBar.hide();
                            modleView.progressBar.progressbar('setValue', 0);
                        }
                    });
                },
                onLoadError: function () {
                    $.messager.show({
                        title: "提示",
                        msg: "网络出错！",
                        timeout: 3000,
                        showType: 'slide',
                        height: 200,
                        width: 300
                    });
                }
            });
        },
        clearForm: function () {
            $('#patientForm').form('clear');
        },
        initial: function () {
            //初始化选择框
            $("#hospitalId").combobox({
                url: '/patient/getHospital',
                mode: 'remote',
                method: 'post',
                valueField: '_id',
                textField: 'name',
                panelWidth: 350,
                panelHeight: 'auto',
                label: '医院:',
                labelPosition: 'left',
                required: true,
                onSelect: function (rec) {
                    $('#prefix').textbox("setValue", rec.prefix);
                },
                formatter: function (row) {
                    var s = '<span style="font-weight:bold">' + row.name + '</span><br/>' +
                        '<span style="color:#888">' + row._id + '</span>';
                    return s;
                }
            });
            //格式化日期值
            $('#batchNoDateBox').datebox({
                formatter: function (date) {
                    var y = date.getFullYear();
                    var m = date.getMonth() + 1;
                    var d = date.getDate();
                    if (m < 10) {
                        m = "0" + m;
                    }
                    if (d < 10) {
                        d = "0" + d;
                    }
                    return "" + y + m + d;
                },
                parser: function (dateStr) {
                    var year = dateStr.substring(0, 4);
                    var month = dateStr.substring(4, 6);
                    var day = dateStr.substring(6, 8);
                    var t = Date.parse(year + "-" + month + "-" + day);
                    if (!isNaN(t)) {
                        return new Date(t);
                    } else {
                        return new Date();
                    }
                }

            });
            modleView.progressBar.hide();
        }
    };

    //initial
    modleView.initial();
    //bind click
    modleView.submitBtn.bind("click", modleView.submitForm);
    modleView.clearBtn.bind("click", modleView.clearForm);

});
