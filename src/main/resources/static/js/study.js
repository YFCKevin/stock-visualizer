function loadData() {
  return {
    currentStep: 1,
    studyId: "686a2e8704f1b409dce58ad8",
    steps: [],
    contents: [],

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

      $.ajax({
        url: "study/" + this.studyId + "/contents",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
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