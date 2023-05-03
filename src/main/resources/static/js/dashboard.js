let requestData;
const taskUrlDash = "/api/task";
const taskSelector = '.task--body';
const headersWithJwt = {
    "Authorization": 'Bearer ' + localStorage.getItem("accessToken")
}

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: taskUrlDash,
    pageName: "index"
}

function setEventListeners() {
    document.querySelectorAll(taskSelector)
        .forEach(task =>
            task.addEventListener('click', openModal));
}

setEventListeners();

function openModal(evt) {
    evt.stopPropagation();
    const id = evt.target
        .closest(taskSelector)
        .getAttribute('value');
    modalFill(id);
}

function modalFill(id) {
    form = $('#detailsForm');
    $.ajaxSetup({
        headers: headersWithJwt
    });
    $.get(ctx.ajaxUrl + '/' + id, function (data) {
        requestData = data;
        $.each(data, function (key, valueKey) {
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

function sendDashboardForm() {
    let data = form.serialize();
    $.ajax({
        type: "POST",
        url: ctx.ajaxUrl + '/form',
        data: data,
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Authorization", 'Bearer ' + localStorage.getItem("accessToken"));
        }
    }).done(function () {
        $("#editRow").modal('hide');
        updateTable()
        successNoty("Record saved");
        setTimeout(() => {
            window.location = window.location.origin + '/';
        }, 1000);

    });
}
