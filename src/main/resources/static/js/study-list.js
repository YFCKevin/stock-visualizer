function loadData() {
  return {
    studies: [],
    editStudyId: "",
    title: "",
    desc: "",

    init(){
      let _this = this;

      $.ajax({
        url: "member/info",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.member = response;
          console.log(_this.member);
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });

      this.getAllStudies();

    },
    getAllStudies() {
      let _this = this;
      $.ajax({
        url: "study",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            _this.studies = response;
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    openAddStudyModal(){
      $("#addStudyModal").modal("show");
    },
    createStudy(){
      let _this = this;
      let data = {};
      data.title = this.title;
      data.desc = this.desc;

      $.ajax({
        url: "study",
        type: "post",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
            _this.studies.push(response);
            $("#addStudyModal").modal("hide");
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    opnEditStudyModal(studyId){
      let _this = this;
      $.ajax({
        url: "study/" + studyId,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          $("#editTitle").val(response.title);
          $("#editDesc").val(response.desc);
          _this.editStudyId = response.id;
          $("#editStudyModal").modal("show");
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    editStudy(){
      let _this = this;
      let data = {};
      data.id = this.editStudyId;
      data.title = $("#editTitle").val();
      data.desc = $("#editDesc").val();
      $.ajax({
        url: "study/edit",
        type: "patch",
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
          _this.getAllStudies();
          $("#editStudyModal").modal("hide");
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    openArchiveStudyModal(studyId){
      let _this = this;
      $.ajax({
        url: "study/" + studyId,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          $("#archiveStudyModal").modal("show");
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    archiveStudy(){
      let _this = this;
      $.ajax({
        url: "study/archive/" + studyId,
        type: "post",
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.getAllStudies();
          $("#archiveStudyModal").modal("hide");
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
  };
}