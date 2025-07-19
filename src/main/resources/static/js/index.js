function loadData() {
  return {
    stockData: [],
    groupedStockData: [],

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
        url: "symbol",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            const groupedObj = response.reduce((acc, item) => {
              const key = item.symbolTypeLabel || "其他";
              if (!acc[key]) acc[key] = [];
              acc[key].push(item);
              return acc;
            }, {});

            const groupedList = Object.entries(groupedObj).map(([key, items]) => ({
              name: key,
              items: items,
            }));

            _this.groupedStockData = groupedList;
            console.log(groupedList);
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },
    forward(symbol){
      location.href = "layout.html?symbolName=" + symbol;
    },
  };
}