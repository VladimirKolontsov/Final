const taskUrl = "/api/task";

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: taskUrl,
    pageName: "backlog"
}

function enable(chkbox, id) {
    var enabled = chkbox.is(":checked");
//  https://stackoverflow.com/a/22213543/548473
    $.ajax({
        url: taskUrl + '/' + id,
        type: "PATCH",
        data: "enabled=" + enabled,
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Authorization", 'Bearer ' + localStorage.getItem("accessToken"));
        }
    }).done(function () {
        chkbox.closest("tr").attr("data-user-enabled", enabled);
        successNoty(enabled ? "Task enabled" : "Task disabled");
    }).fail(function () {
        $(chkbox).prop("checked", !enabled);
    });
}

$(function () {
    makeEditable({
        "columns": [
            {
                "data": "id"
            },
            {
                "data": "statusCode"
            },
            {
                "data": "title"
            },
            {
                "data": "description",
            },
            {
                "data": "typeCode"
            },
            {
                "data": "enabled",
                "render": function (data, type, row) {
                    if (type === "display") {
                        return "<input type='checkbox' " + (data ? "checked" : "") + " onclick='enable($(this)," + row.id + ");'/>";
                    }
                    return data;
                }
            },
            {
                "orderable": false,
                "defaultContent": "",
                "render": renderEditBtn
            },
            {
                "orderable": false,
                "defaultContent": "",
                "render": renderDeleteBtn
            }
        ],
        "order": [
            [
                0,
                "asc"
            ]
        ],
        "createdRow": function (row, data, dataIndex) {
            if (!data.enabled) {
                $(row).attr("data-user-enabled", false);
            }
        }
    });
});

document.querySelector(".checkbox-value").addEventListener('click', (evt) => {
    let checkbox = evt.target;
    let checked = checkbox.checked;
    checkbox.value = checked ? 'true' : 'false';
});

function renderEditBtn(data, type, row) {
    if (type === "display") {
        return "<a onclick='modalFill(" + row.id + ");'><span class='fa fa-pencil'></span></a>";
    }
}

function modalFill(id) {
    const form = $('#detailsForm');
    $.ajaxSetup({
        headers: {
            "Authorization": 'Bearer ' + localStorage.getItem("accessToken")
        }
    });
    $.get(ctx.ajaxUrl + '/' + id, function (data) {
        $.each(data,     function (key, valueKey) {
            let line = form[0][key];
            if (line) {
                line.value = valueKey;
            }
            if (line != null && line.type === 'checkbox') {
                line.checked = valueKey;
            }
        });
        $('#editRow').modal('show');
    });
}
