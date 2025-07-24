function loadData() {
  return {
    ohlc: [],
    volume: [],
    ohlcIdMap: new Map(),
    KId: null,
    KDate: 0,
    KClose: 0,
    KOpen: 0,
    KHigh: 0,
    KLow: 0,
    KVolume: 0,
    KChange: 0,
    KChangePercent: 0,
    KIsRise: true,
    symbolTitle: "",
    symbolName: "",
    interval: "",
    layoutId: "",
    userSettings: {},
    layoutName: "",
    layoutDesc: "",
    notes: [],
    noteId: "",
    showNote: true,
    DEFAULT_SMA_LIST: [
      { name: "MA5", color: "#4285f4", id: "sma5", checked: false },
      { name: "MA10", color: "#fbbc04", id: "sma10", checked: false },
      { name: "MA20", color: "blue", id: "sma20", checked: true },
      { name: "MA60", color: "red", id: "sma60", checked: true },
      { name: "MA120", color: "#a167e7", id: "sma120", checked: false },
      { name: "MA240", color: "#fb7e3c", id: "sma240", checked: false },
      { name: "MA260", color: "green", id: "sma260", checked: true },
    ],
    layouts: [],
    delLayoutId: "",
    searchLayoutKeyword: "",
    unsavedLabel: false,

    // 新聞
    news: [],
    newsTitle: '',
    newsTags: [],
    tagOptions: [
      { value: "OIL_PRICE", label: "油價" },
      { value: "INFLATION", label: "通膨" },
      { value: "INTEREST_RATE", label: "利率" },
      { value: "FOREIGN_EXCHANGE", label: "匯率" },
      { value: "ECONOMY", label: "經濟" },
      { value: "STOCK_MARKET", label: "股市" },
      { value: "BOND_MARKET", label: "債市" },
      { value: "HOUSING_MARKET", label: "房市" },
      { value: "CURRENT_EVENTS", label: "時事" },
    ],
    newsUrl: '',
    accessRuleId: '',
    ruleOptions: [],
    newsPublishedAt: '',
    allMembers: [],
    newsDeleteId: '',
    currentNewsDate: '',
    editNewsId: '',

    // rule
    ruleTitle: '',
    rules: [],
    ruleStatus: 'add',
    ruleSearchResults: [],
    selectedVisibleMembers: [],
    ruleSearchQuery: '',
    currentEditingRuleId: '',
    ruleDeleteId: '',
    viewRuleName: '',
    viewRuleMembers: [],

    init() {
      const params = new URLSearchParams(window.location.search);
      this.layoutId = params.get("id");
      this.symbolName = params.get("symbolName");

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

      if (this.layoutId) params.set("id", this.layoutId);
      if (this.symbolName) params.set("symbolName", this.symbolName);
      this.initLayoutAndNote(params);
    },

    createNewLayout() {
      let data = {};
      data.symbol = this.symbolName;
      data.interval = this.interval;
      fetch("layout", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (response.status === 401) {
            window.location.href = "login.html";
            return;
          }
          if (!response.ok) {
            throw new Error(
              "Network response was not ok: " + response.statusText
            );
          }
          return response.text();
        })
        .then((layoutId) => {
          window.location.href = "layout.html?id=" + layoutId;
        })
        .catch((error) => {
          alert("發生錯誤，請稍後再試");
        });
    },

    copyLayout(){
      let _this = this;
      fetch("layout/copy/" + this.layoutId, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      })
        .then((response) => {
          if (response.status === 401) {
            window.location.href = "login.html";
            return;
          }
          if (!response.ok) {
            throw new Error(
              "Network response was not ok: " + response.statusText
            );
          }
          return response.text();
        })
        .then((layoutId) => {
          window.location.href = "layout.html?id=" + layoutId;
        })
        .catch((error) => {
          alert("發生錯誤，請稍後再試");
        });
    },

    initLayoutAndNote(params) {
      let _this = this;
      $.ajax({
        url: `layout/enter?${params.toString()}`,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.layoutName = response.name;
          _this.layoutDesc = response.desc;
          _this.interval = response.interval;
          _this.layoutId = response.id;
          _this.symbolName = response.symbol;
          _this.userSettings = response.userSettings || {};

          // 取筆記內容
          _this.getNoteData();

          _this.symbolTitle = response.ohlcDataDTOList[0].symbolName;

          response.ohlcDataDTOList.forEach((item) => {
            const date = item.date;
            const open = item.open;
            const high = item.high;
            const low = item.low;
            const close = item.close;
            const symbol = item.symbol;
            const interval = item.interval;
            const id = item.id + "_" + date;

            const timestamp = new Date(date).getTime();

            _this.ohlc.push([timestamp, open, high, low, close]);

            // 存下 id 對應 timestamp
            _this.ohlcIdMap.set(timestamp, id);

            _this.volume.push({
              x: timestamp,
              y: item.volume,
              color: close > open ? "red" : "green",
            });
          });

          const chart = _this.initChart(_this.ohlc, _this.volume);
          window.chart = chart;

          const smaList = _this.userSettings?.smaList || _this.DEFAULT_SMA_LIST;

          smaList.forEach((ma) => {
            if (ma.checked) {
              const period = parseInt(ma.id.replace("sma", ""), 10);
              _this.addSMA(period, ma.color);
            }
          });

          // 顯示最後一筆股價的資料
          const last = _this.ohlc[_this.ohlc.length - 1];
          const [ts, open, high, low, close] = last;
          const lastVolumeObj = _this.volume.find((v) => v.x === ts) || {
            y: 0,
          };
          const volumeVal = lastVolumeObj.y;

          const change = close - open;
          const changePercent = (change / open) * 100;
          const isRise = change >= 0;

          _this.KDate = Highcharts.dateFormat("%Y-%m-%d", ts);
          _this.KOpen = open;
          _this.KHigh = high;
          _this.KLow = low;
          _this.KClose = close;
          _this.KVolume = volumeVal;
          _this.KChange = change;
          _this.KChangePercent = changePercent;
          _this.KIsRise = isRise;
        },
      });
    },

    getNoteData() {
      let _this = this;
      $.ajax({
        url: `note/layout/${this.layoutId}`,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.notes = response.map((note) => {
            const date = new Date(note.createdAt);
            const formattedDate = date.toISOString().slice(0, 10); // yyyy-MM-dd

            const title = note.title || "(無標題)";

            return {
              ...note,
              info: `${formattedDate} ${title}`,
            };
          });
          let noteSize = _this.notes.length;
          _this.noteId = _this.notes[noteSize - 1].id;

          _this.$nextTick(() => {
            if (typeof initEditor === 'function') {
              initEditor(_this.notes[noteSize - 1].content);
            }
          });
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          } else if (xhr.status === 404){ // 找不到對應的 Note 資料
            _this.$nextTick(() => {
              if (typeof initEditor === 'function') {
                initEditor("");
              }
            });
          }
        },
      });
    },

    nextNote(){
      const currentIndex = this.notes.findIndex(note => note.id === this.noteId);
      if (currentIndex < this.notes.length - 1) {
        this.noteId = this.notes[currentIndex + 1].id;
        window.editor.setData(this.notes[currentIndex + 1].content);
      }
    },

    previewNote(){
      const currentIndex = this.notes.findIndex(note => note.id === this.noteId);
      if (currentIndex > 0) {
        this.noteId = this.notes[currentIndex - 1].id;
        window.editor.setData(this.notes[currentIndex - 1].content);
      }
    },

    openEditNoteModal(){
      $("#editNoteModal").modal("show");
      const note = this.notes.find((n) => n.id === this.noteId);
      $("#noteName").val(note.title);
    },

    editNoteTitle(){
      let _this = this;
      let data = {};
      data.id = this.noteId;
      data.title = $("#noteName").val();
      $.ajax({
        url: "note",
        type: "patch",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
          console.log(response)
          const index = _this.notes.findIndex(note => note.id === response.id);
          if (index !== -1) {
            _this.notes[index] = response;
          } else {
            _this.notes.push(response);
          }
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },

    openDeleteNoteModal(){
      $("#noteDeleteModal").modal("show");
    },

    deleteNote(){
      let _this = this;
      $.ajax({
        url: "note/" + this.noteId,
        type: "delete",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.notes = _this.notes.filter(note => note.id !== _this.noteId);
          let noteSize = _this.notes.length;
          _this.noteId = _this.notes[noteSize - 1].id;
          console.log(_this.notes[noteSize - 1])
          window.editor.setData(_this.notes[noteSize - 1].content);
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },

    isFirstNote() {
      return this.notes.findIndex(note => note.id === this.noteId) === 0;
    },
    isLastNote() {
      return this.notes.findIndex(note => note.id === this.noteId) === this.notes.length - 1;
    },

    createNote(){
      this.noteId = "";
      window.editor.setData("");
      let data = {};
      data.id = this.noteId;
      data.layoutId = this.layoutId;
      data.content = window.editor.getData();
      let _this = this;
      $.ajax({
        url: "note/save",
        type: "post",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
          console.log(response);
          _this.notes.push(response)
          let noteSize = _this.notes.length;
          _this.noteId = _this.notes[noteSize - 1].id;
          window.editor.setData(_this.notes[noteSize - 1].content);
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },

    openAddNewsModal(){
      this.cleanAddNewsModal();
      $("#addNewsModal").modal("show");
      // 取得會員的權限列表
      this.getAllRules();
    },

    cleanAddNewsModal(){
      this.newsTitle = "";
      this.newsTags = [];
      this.newsUrl = "";
      this.newsPublishedAt = "";
      if (this.ruleOptions.length > 0) {
        this.accessRuleId = this.ruleOptions[0].id;
      }
    },

    getAllNews(date){
      let _this = this;
      $.ajax({
        url: "news/date/" + date,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.news = response;
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    backModal(){
      $("#addNewsModal").modal("show");
      $("#ruleModal").modal("hide");
    },

    filterMembers() {
      const q = this.ruleSearchQuery.toLowerCase();
      this.ruleSearchResults = this.allMembers.filter(
          m => m.name.toLowerCase().includes(q) &&
            !this.selectedVisibleMembers.some(s => s.id === m.id)
      );
    },
    addMember(member) {
      this.selectedVisibleMembers.push(member);
      this.filterMembers();
    },
    removeMember(id) {
      const index = this.selectedVisibleMembers.findIndex(member => member.id === id);
      if (index !== -1) {
        this.selectedVisibleMembers.splice(index, 1);
        this.filterMembers();
      }
    },

    saveNews() {
      let _this = this;

      let data = {
        id: this.editNewsId,
        title: this.newsTitle,
        tags: this.newsTags,
        url: this.newsUrl,
        publishedAt: new Date(this.newsPublishedAt).getTime(),
        accessRuleId: this.accessRuleId
      };

      // 判斷是否編輯
      const isEdit = this.editNewsId && this.editNewsId.trim() !== "";

      $.ajax({
        url: isEdit ? "news/edit" : "news",
        type: isEdit ? "patch" : "post",
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);

          if (_this.currentNewsDate !== "") {
            _this.getAllNews(new Date(_this.currentNewsDate).getTime());
          }

          $("#addNewsModal").modal("hide");
        },
        error: function (xhr) {
          console.log(xhr);
          console.log("系統異常，請稍後再試");
        }
      });
    },

    onDateChange(){
      if (this.currentNewsDate !== "") {
        this.getAllNews(new Date(this.currentNewsDate).getTime());
      }
    },

    saveRule(){
      let _this = this;
      let data = {};
      data.ruleName = this.ruleTitle;
      data.visibility = "RESTRICTED";
      data.visibleToMemberIds = this.selectedVisibleMembers.map(member => member.id);

      console.log(data)

      $.ajax({
        url: "news/rule",
        type: "post",
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.rules.push(response);

          console.log(_this.rules)

          _this.selectedVisibleMembers = [];
          _this.filterMembers();
          _this.ruleTitle = "";
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    openRuleModal(){
      $("#ruleModal").modal("show");
      $("#addNewsModal").modal("hide");

      let _this = this;

      // 取得所有會員列表
      $.ajax({
        url: "member",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.ruleSearchResults = response;
          _this.allMembers = response;
          console.log(_this.ruleSearchResults);
        },
        error: function (xhr, status, error) {
          if (xhr.status === 401) {
            // 401 Unauthorized
            window.location.href = "login.html";
          }
        },
      });
    },

    getAllRules(){
      let _this = this;
      // 取得會員的權限列表
      return $.ajax({
        url: "news/rule",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);

          _this.rules = response
            .filter(rule => rule.visibility !== 'PRIVATE' && rule.visibility !== 'PUBLIC');

          _this.ruleOptions = response.map(rule => ({
            id: rule.id,
            ruleName: rule.visibility === 'RESTRICTED'
              ? `指定觀看 - (${rule.ruleName})`
              : rule.ruleName
          }));

          if (_this.ruleOptions.length > 0) {
            _this.accessRuleId = _this.ruleOptions[0].id;
          }
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    viewRule(ruleId){
      let _this = this;
      $.ajax({
        url: "news/rule/" + ruleId,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.viewRuleName = response.ruleName;
          _this.viewRuleMembers = response.visibleToMember.map(member => ({
            id: member.id,
            name: member.name
          }));
          $("#viewRuleModal").modal("show");
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    handleRuleClick() {
      if (this.ruleStatus === 'edit') {
        this.confirmEdit();
      } else {
        this.saveRule();
      }
    },

    startEdit(ruleId) {
      // 如果正在編輯同一筆，則取消編輯狀態
      if (this.currentEditingRuleId === ruleId) {
        this.currentEditingRuleId = '';
        this.ruleStatus = 'add';
        this.ruleTitle = '';
        this.selectedVisibleMembers = [];
        this.ruleSearchQuery = '';
        _this.filterMembers();
        return;
      }

      // 切換到新的編輯
      const targetRule = this.rules.find(rule => rule.id === ruleId);
      if (targetRule) {
        this.currentEditingRuleId = ruleId;
        this.ruleStatus = "edit";
        this.ruleTitle = targetRule.ruleName;
        this.selectedVisibleMembers = targetRule.visibleToMember.map(member => ({
          id: member.id,
          name: member.name
        }));
        this.ruleSearchQuery = "";
        this.filterMembers();
      } else {
        console.warn('找不到指定的規則：', ruleId);
      }
    },

    confirmEdit(){
      let _this = this;
      let data = {};
      data.id = this.currentEditingRuleId;
      data.ruleName = this.ruleTitle;
      data.visibility = "RESTRICTED";
      data.visibleToMemberIds = this.selectedVisibleMembers.map(member => member.id);

      console.log(data)

      $.ajax({
        url: "news/rule",
        type: "put",
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.selectedVisibleMembers = [];
          _this.filterMembers();
          _this.ruleTitle = "";
          _this.getAllRules();
          _this.currentEditingRuleId = "";
          _this.ruleStatus = "add";
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    openDeleteRuleModal(ruleId){
      this.ruleDeleteId = ruleId;
      $("#ruleDeleteModal").modal("show");
    },

    deleteRule(){
      let _this = this;
      $.ajax({
        url: "news/rule/" + this.ruleDeleteId,
        type: "delete",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.currentEditingRuleId = '';
          _this.ruleStatus = 'add';
          _this.ruleTitle = '';
          _this.selectedVisibleMembers = [];
          _this.ruleSearchQuery = '';
          _this.filterMembers();
          _this.getAllRules();
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },
    openDeleteNewsModal(id){
      this.newsDeleteId = id;
      $("#newsDeleteModal").modal("show");
    },

    startEditNews(id){
      this.editNewsId = id;
      let _this = this;
      $.ajax({
        url: "news/" + this.editNewsId,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.newsTitle = response.title;
          _this.newsTags = response.tags;
          _this.newsUrl = response.url;
          _this.newsPublishedAt = new Date(response.publishedAt).toISOString().slice(0, 10);

          _this.getAllRules().then(() => {
            _this.accessRuleId = response.accessRuleId;
            $("#addNewsModal").modal("show");
          });

        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    deleteNews(){
      let _this = this;
      $.ajax({
        url: "news/" + this.newsDeleteId,
        type: "delete",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          if (_this.currentNewsDate != "") {
            _this.getAllNews(new Date(_this.currentNewsDate).getTime());
          }
          $("#newsDeleteModal").modal("hide");
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    openEditLayoutModal() {
      $("#layoutName").val(this.layoutName);
    },

    editLayoutName() {
      let _this = this;
      let name = $("#layoutName").val();
      let data = {};
      data.name = name;
      data.id = this.layoutId;
      $.ajax({
        url: "layout/edit",
        type: "patch",
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.layoutName = response.name;
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    openLayoutSearchModal() {
      let _this = this;
      $.ajax({
        url: "layout/search",
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.layouts = response.map((layout) => {
            const timestamp = layout.updateAt || layout.createAt;
            const formattedDate = _this.formatTimestamp(timestamp);

            return {
              ...layout,
              formattedDate: formattedDate,
              info:
                layout.symbolName +
                " (" +
                layout.symbol +
                ")" +
                ", " +
                formattedDate,
              checkedStyle: layout.id == _this.layoutId,
            };
          });
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    openDeleteLayoutModal(layoutId) {
      console.log("delete layout");
      this.delLayoutId = layoutId;
      $("#deleteModal").modal("show");
    },

    deleteLayout() {
      let _this = this;
      $.ajax({
        url: "layout/" + this.delLayoutId,
        type: "delete",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.searchLayout();
          $("#deleteModal").modal("hide");
        },
      }).fail(function (xhr) {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    searchLayout() {
      console.log(this.searchLayoutKeyword);
      let _this = this;
      $.ajax({
        url: "layout/search?keyword=" + this.searchLayoutKeyword,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.layouts = response.map((layout) => {
            const timestamp = layout.updateAt || layout.createAt;
            const formattedDate = _this.formatTimestamp(timestamp);
            return {
              ...layout,
              formattedDate: formattedDate,
              info:
                layout.symbolName +
                " (" +
                layout.symbol +
                "), " +
                formattedDate,
              checkedStyle: layout.id == _this.layoutId,
            };
          });
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },

    selectLayout(id) {
      location.href = "layout.html?id=" + id;
    },

    addSMA(period, color = "blue") {
      if (chart.get(`sma-${period}`)) return; // 已存在就不加
      chart.addSeries({
        type: "sma",
        linkedTo: "price",
        name: `SMA(${period})`,
        params: { period },
        marker: { enabled: false },
        states: { hover: { enabled: false } },
        color,
        lineWidth: 1.5,
        id: `sma-${period}`,
        showInNavigator: false,
        dataGrouping: { enabled: false },
        tooltip: { enabled: false },
      });
    },

    initChart(ohlc, volume) {
      let _this = this;
      if (_this.chart) {
        _this.chart.destroy();
        window.chart.destroy();
      }
      const chart = Highcharts.stockChart("container", {
        stockTools: {
          gui: {
            enabled: true,
            iconsURL: "/stock/icons/",
            buttons: [
              //              "indicators",
              "simpleShapes",
              "lines",
              "crookedLines",
              "verticalLabels",
              "measure",
              "toggleAnnotations",
            ],
            definitions: {
              lines: {
                items: ["segment", "arrowSegment", "horizontalLine"],
              },
              crookedLines: {
                items: ["crooked3", "elliott3", "elliott5"],
              },
              simpleShapes: {
                items: ["label", "circle", "rectangle"],
              },
              verticalLabels: {
                items: ["verticalLabel"],
              },
            },
          },
        },
        navigation: {
          buttonOptions: {
            enabled: false, // 隱藏右上角 menu
          },
          annotationsOptions: {
            shapeOptions: {
              //              dashStyle: 'Dash',    // 虛線
              stroke: "#000000",
              strokeWidth: 2,
              fill: "none",
            },
            labelOptions: {
              backgroundColor: "rgba(0, 0, 0, 0)", // 透明: transparent
              borderWidth: 0,
              style: {
                color: "rgba(0, 0, 0, 1)",
                fontWeight: "bold",
                fontSize: "12px",
              },
            },
          },
        },
        // 設定K棒樣式
        plotOptions: {
          candlestick: {
            color: "red",
            lineColor: "red",
            upColor: "white",
            upLineColor: "black",
          },
          series: {
            findNearestPointBy: "x",
            cursor: "pointer",
            point: {
              events: {
                click: function () {
                  console.log("指定k bar對應的新聞");
                  const fullId = _this.ohlcIdMap.get(this.x);
                  if (fullId && fullId.includes('_')) {
                    const [idPart, datePart] = fullId.split('_');
                    _this.newsPublishedAt = datePart;
                  }
                },
              },
            },
          },
        },
        xAxis: {
          type: "datetime",
          ordinal: true,
          crosshair: {
            color: "#343a40",
            dashStyle: "Dash",
            width: 1,
            label: {
              enabled: true,
              formatter: function (value) {
                return Highcharts.dateFormat("%Y/%m/%d", value);
              },
              backgroundColor: "#343a40",
              style: {
                color: "#fff",
                fontSize: "14px",
                fontWeight: "bold",
              },
            },
          },
          labels: {
            formatter: function () {
              const axis = this.axis;
              const chart = this.chart;

              const min = axis.min;
              const max = axis.max;
              const oneYear = 365 * 24 * 3600 * 1000;
              const isLongRange = max - min > oneYear;
              const format = isLongRange ? "%Y/%m" : "%m/%d";
              return Highcharts.dateFormat(format, this.value);
            },
            style: {
              fontSize: "12px",
              color: "#333333",
            },
          },
        },
        yAxis: [
          {
            crosshair: {
              color: "#343a40",
              dashStyle: "Dash",
              width: 1,
              label: {
                enabled: true,
                formatter: function (value) {
                  if (value < 1000) {
                    return value.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    });
                  } else {
                    return value.toLocaleString(undefined, {
                      maximumFractionDigits: 0,
                    });
                  }
                },
                backgroundColor: "#343a40",
                style: {
                  color: "#fff",
                  fontSize: "14px",
                  fontWeight: "bold",
                },
              },
            },
            labels: {
              align: "left",
              formatter: function () {
                return this.value.toLocaleString();
              },
              style: {
                fontSize: "12px",
                color: "#000000",
              },
            },
            height: "49%",
            resize: {
              enabled: false,
            },
          },
          {
            top: "52%",
            height: "8%",
            labels: {
              enabled: false,
            },
            offset: 0,
            min: -2,
            max: 102,
            plotLines: [
              {
                value: 80,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "80",
                  align: "right",
                  x: 20,
                  y: 3,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
              {
                value: 50,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Dash",
                label: {
                  text: "50",
                  align: "right",
                  x: 20,
                  y: 3,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
              {
                value: 20,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "20",
                  align: "right",
                  x: 20,
                  y: 3,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
            ],
          },
          {
            top: "62%",
            height: "8%",
            labels: {
              enabled: false,
            },
            offset: 0,
            min: -400,
            max: 400,
            plotLines: [
              {
                value: 100,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "100",
                  align: "right",
                  x: 25,
                  y: 1,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
              {
                value: 0,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Dash",
                label: {
                  text: "0",
                  align: "right",
                  x: 25,
                  y: 3,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
              {
                value: -100,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "-100",
                  align: "right",
                  x: 25,
                  y: 5,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
            ],
          },
          {
            top: "72%",
            height: "8%",
            labels: {
              enabled: false,
            },
            offset: 0,
          },
          {
            top: "82%",
            height: "8%",
            labels: {
              enabled: false,
            },
            offset: 0,
            plotLines: [
              {
                value: 40,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "40",
                  align: "right",
                  x: 20,
                  y: 1,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
              {
                value: 20,
                color: "rgba(128, 128, 128, 1)",
                width: 1,
                dashStyle: "Solid",
                label: {
                  text: "20",
                  align: "right",
                  x: 20,
                  y: 5,
                  style: {
                    color: "rgba(128, 128, 128, 1)",
                    fontSize: "10px",
                  },
                },
                zIndex: 3,
              },
            ],
          },
          {
            labels: {
              enabled: false,
            },
            top: "92%",
            height: "8%",
            offset: 0,
          },
        ],
        scrollbar: {
          enabled: false,
        },
        navigator: {
          xAxis: {
            labels: {
              formatter: function () {
                return Highcharts.dateFormat("%Y/%m", this.value);
              },
            },
          },
        },
        rangeSelector: {
          //          enabled: false,
          labelStyle: {
            display: "none", // 隱藏 Zoom
          },
          inputEnabled: false,
          selected: 0,
          buttons: [
            { type: "month", count: 3, text: "3個月" },
            { type: "month", count: 6, text: "6個月" },
            { type: "month", count: 12, text: "12個月" },
          ],
          buttonPosition: {
            align: "left", // 靠左對齊
            verticalAlign: "top",
            x: -20, // 往左微調避免貼邊
            y: -13,
          },
          buttonTheme: {
            fill: "#f0f0f0",
            stroke: "none",
            "stroke-width": 1,
            r: 4,
            padding: 8,
            style: {
              color: "#333333",
              fontWeight: "normal",
            },
            states: {
              hover: {
                fill: "#e6e6e6",
              },
              select: {
                fill: "#e6e6e6", // 被選取時的底色
                style: {
                  color: "#333333",
                  fontWeight: "bold",
                },
              },
            },
          },
        },
        tooltip: {
          shared: true,
          split: false,
          snap: true,
          shape: "square",
          headerShape: "callout",
          borderWidth: 0,
          shadow: false,
          fixed: true,
          xDateFormat: "%Y-%m-%d",
          formatter: function () {
            const timestamp = this.x;
            const id = _this.ohlcIdMap.get(timestamp);

            const dateStr = Highcharts.dateFormat(
              this.options.xDateFormat || "%Y-%m-%d",
              this.x
            );

            const candle = this.points?.find(
              (p) => p.series.type === "candlestick"
            );

            const volume = this.points?.find((p) => p.series.type === "column");
            const mv5 = this.points.filter((p) => p.series.name === "MV5")[0];
            const mv20 = this.points.filter((p) => p.series.name === "MV20")[0];
            const sma5 = this.points.filter(
              (p) => p.series.name === "SMA(5)"
            )[0];
            const sma10 = this.points.filter(
              (p) => p.series.name === "SMA(10)"
            )[0];
            const sma20 = this.points.filter(
              (p) => p.series.name === "SMA(20)"
            )[0];
            const sma60 = this.points.filter(
              (p) => p.series.name === "SMA(60)"
            )[0];
            const sma120 = this.points.filter(
              (p) => p.series.name === "SMA(120)"
            )[0];
            const sma240 = this.points.filter(
              (p) => p.series.name === "SMA(240)"
            )[0];
            const sma260 = this.points.filter(
              (p) => p.series.name === "SMA(260)"
            )[0];
            const cci = this.points.filter(
              (p) => p.series.name === "CCI (14)"
            )[0];
            const stochastic = this.points.filter(
              (p) => p.series.name === "Stochastic (14,3)"
            )[0];
            const macd = this.points.filter(
              (p) => p.series.name === "MACD (26, 12, 9)"
            )[0];
            const dmi = this.points.filter(
              (p) => p.series.name === "DMI (14)"
            )[0];
            if (!candle) return false;

            const close = candle.point.close;
            const open = candle.point.open;
            const high = candle.point.high;
            const low = candle.point.low;

            // 計算漲跌
            const change = close - open;
            const changePercent = (change / open) * 100;
            const isRise = change >= 0;

            // 更新綁定資料
            _this.KDate = dateStr;
            _this.KClose = close;
            _this.KOpen = open;
            _this.KHigh = high;
            _this.KLow = low;
            _this.KVolume = volume.y;
            _this.KChange = change;
            _this.KChangePercent = changePercent;
            _this.KIsRise = isRise;
            _this.KId = candle.point.options.id || null;

            if (stochastic?.point) {
              const kValue = stochastic.options.y;
              const dValue = stochastic.options.smoothed;

              if (_this.kdLabel) {
                _this.kdLabel.attr({
                  useHTML: true,
                  text: `
                    <tspan style="fill:#000000;">KD</tspan>
                    <tspan style="fill:#333;">   K </tspan>
                    <tspan style="font-weight:bold; fill:#333;">${kValue.toFixed(
                      2
                    )}</tspan>
                    <tspan style="fill:red;"> D </tspan>
                    <tspan style="font-weight:bold; fill:red;">${dValue.toFixed(
                      2
                    )}</tspan>
                  `,
                });
              }
            }

            if (cci?.point) {
              const cciValue = cci.options.y;

              if (_this.cciLabel) {
                _this.cciLabel.attr({
                  useHTML: true,
                  text: `
                    <tspan style="fill:#000000;">CCI</tspan>
                    <tspan style="fill:#333;">   CCI </tspan>
                    <tspan style="font-weight:bold; fill:#333;">${cciValue.toFixed(
                      2
                    )}</tspan>
                  `,
                });
              }
            }

            if (macd?.point) {
              const macdValue = macd.options.MACD;
              const difValue = macd.options.signal;
              const oscValue = macd.options.y;

              if (_this.macdLabel) {
                _this.macdLabel.attr({
                  useHTML: true,
                  text: `
                    <tspan style="fill:#000000;">MACD</tspan>
                    <tspan style="fill:#333;">  MACD </tspan>
                    <tspan style="font-weight:bold; fill:#333;">${macdValue.toFixed(
                      2
                    )}</tspan>
                    <tspan style="fill:red;"> DIF </tspan>
                    <tspan style="font-weight:bold; fill:red;">${difValue.toFixed(
                      2
                    )}</tspan>
                    <tspan style="fill:#1e90ff;"> OSC </tspan>
                    <tspan style="font-weight:bold; fill:#1e90ff;">${oscValue.toFixed(
                      2
                    )}</tspan>
                  `,
                });
              }
            }

            if (dmi?.point) {
              const minusDIValue = dmi.options.minusDI;
              const plusDIValue = dmi.options.plusDI;
              const adxValue = dmi.options.y;

              if (_this.dmiLabel) {
                _this.dmiLabel.attr({
                  useHTML: true,
                  text: `
                    <tspan style="fill:#000000;">DMI</tspan>
                    <tspan style="fill:#333;">   ADX </tspan>
                    <tspan style="font-weight:bold; fill:#333;">${adxValue.toFixed(
                      2
                    )}</tspan>
                    <tspan style="fill:green;"> +DI </tspan>
                    <tspan style="font-weight:bold; fill:green;">${plusDIValue.toFixed(
                      2
                    )}</tspan>
                    <tspan style="fill:red;"> -DI </tspan>
                    <tspan style="font-weight:bold; fill:red;">${minusDIValue.toFixed(
                      2
                    )}</tspan>
                  `,
                });
              }
            }

            if (_this.volumeLabel && mv5 && mv20 && volume) {
              const mv5Value = mv5.y;
              const mv20Value = mv20.y;
              const volumeValue = volume.y;

              _this.volumeLabel.attr({
                useHTML: true,
                text: `
                  <tspan class="ms-3" style="font-weight:bold; color:#000000;">成交量</tspan>
                  <tspan style="fill:#1e90ff;">   MV5 </tspan>
                  <tspan style="font-weight:bold; fill:#1e90ff;">${mv5Value.toLocaleString(
                    undefined,
                    { maximumFractionDigits: 0 }
                  )}</tspan>
                  <tspan style="fill:#e91e63;"> MV20 </tspan>
                  <tspan style="font-weight:bold; fill:#e91e63;">${mv20Value.toLocaleString(
                    undefined,
                    { maximumFractionDigits: 0 }
                  )}</tspan>
                  <tspan style="fill:#333;"> 量 </tspan><tspan style="font-weight:bold; fill:#333;">${volumeValue.toLocaleString()}</tspan>
                `,
              });
            }

            const texts = _this.smaList
              .filter((ma) => ma.checked)
              .map((ma) => {
                const period = parseInt(ma.id.replace("sma", ""));
                const point = this.points.find(
                  (p) => p.series.name === `SMA(${period})`
                );
                if (!point) return "";
                return `</i><span style="color:${ma.color}; font-size:12px;">${
                  ma.name
                }: ${point.y.toFixed(1)}</span>`;
              })
              .filter(Boolean);

            return texts.join("<br>");
          },
        },
        series: [
          {
            type: "candlestick",
            id: "price",
            name: name,
            data: ohlc,
            dataGrouping: {
              enabled: false,
            },
          },
          {
            type: "stochastic",
            linkedTo: "price",
            yAxis: 1,
            states: {
              hover: {
                enabled: false,
              },
            },
            color: "black", // K 線顏色
            lineWidth: 1.2,
            smoothedLine: {
              styles: {
                lineColor: "red", // D 線顏色
              },
            },
            dataGrouping: {
              enabled: false,
            },
            params: {
              period: 14,
              signalPeriod: 3,
              smoothing: 3,
            },
          },
          {
            type: "cci",
            yAxis: 2,
            linkedTo: "price",
            color: "black",
            lineWidth: 1,
            marker: {
              enabled: false,
            },
            states: {
              hover: {
                enabled: false,
              },
            },
            dataGrouping: {
              enabled: false,
            },
          },
          {
            type: "macd",
            yAxis: 3,
            color: "#0ea5e9",
            linkedTo: "price",
            dataGrouping: {
              enabled: false,
            },
            states: {
              hover: {
                enabled: false,
              },
            },
            macdLine: {
              styles: {
                lineColor: "black",
                lineWidth: 1.5,
              },
            },
            signalLine: {
              styles: {
                lineColor: "red",
                lineWidth: 1,
              },
            },
          },
          {
            type: "dmi",
            yAxis: 4,
            linkedTo: "price",
            color: "black", // ADX 線顏色
            lineWidth: 2, // ADX 線粗細
            marker: {
              enabled: false,
            },
            states: {
              hover: {
                enabled: false,
              },
            },
            dataGrouping: {
              enabled: false,
            },
            params: {
              period: 14,
            },
            styles: {
              plusDILine: {
                lineColor: "green",
                lineWidth: 1,
              },
              minusDILine: {
                lineColor: "red",
                lineWidth: 1,
              },
            },
          },
          {
            type: "column",
            id: "volume",
            name: "Volume",
            data: volume,
            yAxis: 5,
            dataGrouping: {
              enabled: false,
            },
          },
          // MV5 均量線
          {
            type: "sma",
            linkedTo: "volume",
            name: "MV5",
            yAxis: 5,
            color: "blue",
            params: {
              period: 5,
            },
            marker: { enabled: false },
            states: { hover: { enabled: false } },
            lineWidth: 1,
          },
          // MV20 均量線
          {
            type: "sma",
            linkedTo: "volume",
            name: "MV20",
            yAxis: 5,
            color: "red",
            params: {
              period: 20,
            },
            marker: { enabled: false },
            states: { hover: { enabled: false } },
            lineWidth: 1,
          },
        ],
        responsive: {
          rules: [
            {
              condition: {
                maxWidth: 800,
              },
              chartOptions: {
                rangeSelector: {
                  inputEnabled: false,
                },
              },
            },
          ],
        },
        chart: {
          zooming: {
            mouseWheel: { enabled: false },
          },
          panning: false,
          events: {
            load: function () {
              const chart = this;

              // 設定 rangeSelector buttons 動態
              let buttons = [];
              switch (_this.interval) {
                case "1d":
                  buttons = [
                    { type: "month", count: 3, text: "3個月" },
                    { type: "month", count: 6, text: "6個月" },
                    { type: "month", count: 12, text: "12個月" },
                  ];
                  break;
                case "1w":
                  buttons = [
                    { type: "year", count: 1, text: "1年" },
                    { type: "year", count: 3, text: "3年" },
                    { type: "year", count: 5, text: "5年" },
                  ];
                  break;
                case "1m":
                  buttons = [
                    { type: "year", count: 2, text: "2年" },
                    { type: "year", count: 5, text: "5年" },
                    { type: "year", count: 10, text: "10年" },
                  ];
                  break;
              }
              chart.update(
                {
                  rangeSelector: {
                    ...chart.options.rangeSelector,
                    buttons: buttons,
                    selected: 0,
                  },
                },
                false
              );

              // 建立指標標籤（KD, CCI, MACD, DMI, 成交量）
              const kbarLeft = chart.plotLeft;
              const yAxes = chart.yAxis;
              const labelConfigs = [
                { axisIndex: 1, text: "KD", targetVar: "kdLabel" },
                { axisIndex: 2, text: "CCI", targetVar: "cciLabel" },
                { axisIndex: 3, text: "MACD", targetVar: "macdLabel" },
                { axisIndex: 4, text: "DMI", targetVar: "dmiLabel" },
                { axisIndex: 5, text: "成交量", targetVar: "volumeLabel" },
              ];

              labelConfigs.forEach(({ axisIndex, text, targetVar }) => {
                const axis = yAxes[axisIndex];
                if (axis) {
                  _this[targetVar] = chart.renderer
                    .text(text, kbarLeft, axis.top - 1)
                    .css({
                      color: "#000",
                      fontSize: "10px",
                      fontWeight: "bold",
                    })
                    .add();
                }
              });

              // === 建立 SMA 區域 ===
              const container = chart.renderTo;
              const parent = container.parentNode;
              parent.style.position = "relative";

              // 先移除舊的 sma-area
              const oldSmaDiv = document.getElementById("sma-area");
              if (oldSmaDiv) oldSmaDiv.remove();

              // 建立新的 sma-area 容器
              const smaDiv = document.createElement("div");
              smaDiv.id = "sma-area";
              smaDiv.style.position = "absolute";
              smaDiv.style.top = chart.plotTop - 20 + "px";
              smaDiv.style.left = chart.plotLeft + "px";
              smaDiv.style.zIndex = 10;

              parent.appendChild(smaDiv);

              // 取得 smaList（若無則給預設）
              let smaList =
                _this.userSettings.smaList || _this.DEFAULT_SMA_LIST;

              _this.smaList = smaList;

              // 產生 html
              smaDiv.innerHTML = `
            <ul style="display:flex;list-style:none;padding:0;margin:0;gap:12px; font-size:13px;">
              ${smaList
                .map(
                  (ma) => `
                <li style="display:flex;align-items:center;color:${
                  ma.color
                };font-weight:bold;">
                  <input class="me-1 sma-checkbox" type="checkbox" ${
                    ma.checked ? "checked" : ""
                  } id="${ma.id}-checkbox" />
                  <span>${ma.name}</span>
                </li>
              `
                )
                .join("")}
            </ul>
          `;

              // 用事件代理避免重複綁定問題
              $("#sma-area")
                .off("change", ".sma-checkbox")
                .on("change", ".sma-checkbox", function () {
                  const checkbox = this;
                  const period = parseInt(
                    checkbox.id.replace("sma", "").replace("-checkbox", ""),
                    10
                  );
                  const series = chart.get(`sma-${period}`);

                  if (checkbox.checked) {
                    if (!series) {
                      _this.addSMA(
                        period,
                        smaList.find((ma) => ma.id === `sma${period}`)?.color ||
                          "blue"
                      );
                    } else {
                      series.setVisible(true);
                    }
                    smaList.find(
                      (ma) => ma.id === `sma${period}`
                    ).checked = true;
                  } else {
                    if (series) series.setVisible(false);
                    smaList.find(
                      (ma) => ma.id === `sma${period}`
                    ).checked = false;
                  }
                });

              // === 建立繪圖工具區域 ===
              // 先移除舊的 draw-tool-area
              const oldDrawToolDiv = document.getElementById("draw-tool-area");
              if (oldDrawToolDiv) oldDrawToolDiv.remove();

              const drawToolDiv = document.createElement("div");
              drawToolDiv.id = "draw-tool-area";
              drawToolDiv.style.position = "absolute";
              drawToolDiv.style.top = chart.plotTop - 60 + "px";
              drawToolDiv.style.left = chart.plotLeft + 165 + "px";
              drawToolDiv.style.zIndex = 10;
              parent.appendChild(drawToolDiv);

              drawToolDiv.innerHTML = `
            <span class="draw-icon-button" id="btn-solid" data-bs-toggle="tooltip" data-bs-placement="bottom" title="實線" data-container="body" data-animation="true"><img src="/stock/icons/line.svg" alt="solid" /></span>
            <span class="draw-icon-button" id="btn-dash" data-bs-toggle="tooltip" data-bs-placement="bottom" title="虛線" data-container="body" data-animation="true"><img src="/stock/icons/dash.svg" alt="dash" /></span>
            <span class="draw-icon-button" id="btn-arrow" data-bs-toggle="tooltip" data-bs-placement="bottom" title="箭頭實線" data-container="body" data-animation="true"><img src="/stock/icons/arrow.svg" alt="arrow" /></span>
            <span class="draw-icon-button" id="btn-crooked-3" data-bs-toggle="tooltip" data-bs-placement="bottom" title="轉折線" data-container="body" data-animation="true"><img src="/stock/icons/crooked-3.svg" alt="crooked" /></span>
            <span class="draw-icon-button" id="btn-elliott-3" data-bs-toggle="tooltip" data-bs-placement="bottom" title="艾略特校正波浪" data-container="body" data-animation="true"><img src="/stock/icons/elliott-3.svg" alt="elliott3" /></span>
            <span class="draw-icon-button" id="btn-elliott-5" data-bs-toggle="tooltip" data-bs-placement="bottom" title="艾略特三角波浪" data-container="body" data-animation="true"><img src="/stock/icons/elliott-5.svg" alt="elliott5" /></span>
            <span class="draw-icon-button save-icon" id="btn-save" data-bs-toggle="tooltip" data-bs-placement="bottom" title="儲存圖表" data-container="body" data-animation="true"><img src="/stock/icons/save-chart.svg" alt="label" /></span>
          `;

              //            <span class="draw-icon-button" id="btn-circle"><img src="/stock/icons/circle.svg" alt="circle" /></span>
              //            <span class="draw-icon-button" id="btn-rectangle"><img src="/stock/icons/rectangle.svg" alt="rectangle" /></span>
              //            <span class="draw-icon-button" id="label-btn"><img src="/stock/icons/label.svg" alt="label" /></span>

              // 繪圖工具事件綁定
              $("#draw-tool-area")
                .off("click")
                .on("click", ".draw-icon-button", function () {
                  const id = $(this).attr("id");
                  if (!id) return;
                  const type = id.replace(/^btn-/, "");
                  _this.drawing(type);
                });

              $("#draw-tool-area")
                .off("click", "#label-btn")
                .on("click", "#label-btn", function () {
                  $(".highcharts-menu-item-btn").each(function () {
                    const bgImage = $(this).css("background-image");
                    if (bgImage.includes("label.svg")) {
                      $(this).click();
                    }
                  });
                });

              $("#draw-tool-area")
                .off("click", ".save-icon")
                .on("click", ".save-icon", function () {
                  _this.saveAnnotationAndSetting();
                });

              // 設定 dateRange extremes
              if (_this.userSettings.dateRange) {
                const start = _this.userSettings.dateRange.start;
                const end = _this.userSettings.dateRange.end;

                const startDateStr = Highcharts.dateFormat("%Y-%m-%d", start);
                const endDateStr = Highcharts.dateFormat("%Y-%m-%d", end);

                console.log("起始日：", startDateStr);
                console.log("結束日：", endDateStr);

                setTimeout(() => {
                  chart.xAxis[0].setExtremes(start, end);
                }, 50);
              }

              chart.redraw(); // 確保先做 redraw
              setTimeout(() => {
                if (
                  _this.userSettings.annotations &&
                  Array.isArray(_this.userSettings.annotations)
                ) {
                  _this.userSettings.annotations.forEach((annotationConfig) => {
                    chart.addAnnotation(annotationConfig);
                  });
                }
              }, 100);
            },
//            click: function () {
//              console.log(this);
//            },
          },
        },
      });

      return chart;
    },
    callSaveLayoutApi() {
      const data = {
        userSettings: this.userSettings,
        symbol: this.symbolName,
        interval: this.interval,
        name: this.layoutName,
        desc: this.layoutDesc,
      };

      let url = "layout/save";
      let httpType = "POST";

      if (this.layoutId && this.layoutId.trim() !== "") {
        data.id = this.layoutId;
        url = "layout/edit";
        httpType = "PATCH";
      }

      $.ajax({
        url: url,
        type: httpType,
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          alert("成功");
          console.log(response);
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },
    saveAnnotationAndSetting() {
      let _this = this;
      // 從圖表中獲取所有原始 Annotation
      const originalAnnotationConfigs = chart.annotations.map(
        (annotation) => annotation.options
      );
      // 每個 Annotation 加 ID
      const configsWithIds = originalAnnotationConfigs.map((config) => {
        const newConfig = { ...config };
        if (!newConfig.id) {
          newConfig.id = _this.generateUUID();
        }
        return newConfig;
      });
      // 隱藏控制點
      const processedAnnotationConfigs = this.hideControlPoints(configsWithIds);

      let dateRange = this.getDateRange();

      this.userSettings = {
        annotations: processedAnnotationConfigs,
        smaList: this.smaList,
        dateRange: dateRange,
      };

      this.callSaveLayoutApi();
    },
    generateUUID() {
      return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
          var r = (Math.random() * 16) | 0,
            v = c == "x" ? r : (r & 0x3) | 0x8;
          return v.toString(16);
        }
      );
    },
    hideControlPoints(annotations) {
      return annotations.map((annotation) => {
        const options = { ...annotation };
        if (options.typeOptions && options.typeOptions.points) {
          options.typeOptions.points.forEach((point) => {
            if (point.controlPoint) {
              point.controlPoint.visible = false;
            }
          });
        }
        return options;
      });
    },
    drawing(type) {
      fetch("/stock/data/shape.json")
        .then((response) => response.json())
        .then((data) => {
          const indexMap = {
            solid: 0,
            dash: 1,
            arrow: 2,
            "crooked-3": 3,
            "elliott-3": 4,
            "elliott-5": 5,
            circle: 6,
            rectangle: 7,
            label: 8,
          };

          const idx = indexMap[type];
          if (idx === undefined) {
            console.warn("未知的類型:", type);
            return;
          }

          const copiedAnnotation = JSON.parse(JSON.stringify(data[idx]));

          const xAxis = chart.xAxis[0];
          const yAxis = chart.yAxis[0];
          const centerX = (xAxis.min + xAxis.max) / 2;
          const centerY = (yAxis.min + yAxis.max) / 2;

          const typeOpts = copiedAnnotation.typeOptions;
          const shape = copiedAnnotation.shapes?.[0];
          const label = copiedAnnotation.labels?.[0];

          if (Array.isArray(typeOpts?.points)) {
            const originalPoints = typeOpts.points;

            // 1. 原始中心點
            const avgX =
              originalPoints.reduce((sum, p) => sum + p.x, 0) /
              originalPoints.length;
            const avgY =
              originalPoints.reduce((sum, p) => sum + p.y, 0) /
              originalPoints.length;

            // 2. 畫面中心
            const centerX = (chart.xAxis[0].min + chart.xAxis[0].max) / 2;
            const centerY = (chart.yAxis[0].min + chart.yAxis[0].max) / 2;

            // 3. 計算 Y 縱向範圍
            const yValues = originalPoints.map((p) => p.y);
            const minY = Math.min(...yValues);
            const maxY = Math.max(...yValues);
            const height = maxY - minY;

            const yRange = chart.yAxis[0].max - chart.yAxis[0].min;
            const maxHeightRatio = 0.4; // 最多占螢幕40%高
            const maxHeight = yRange * maxHeightRatio;

            let scaleY = 1;
            if (height > maxHeight) {
              scaleY = maxHeight / height;
            }

            // 4. 移動與縮放
            typeOpts.points = originalPoints.map((p) => ({
              ...p,
              x: p.x + (centerX - avgX),
              y: centerY + (p.y - avgY) * scaleY,
            }));
          } else if (shape?.points && Array.isArray(shape.points)) {
            // 取得所有有效點（有 x,y）
            const realPoints = shape.points.filter(
              (p) => p.x !== undefined && p.y !== undefined
            );

            // 算出原本中心點 (avgX, avgY)
            const avgX =
              realPoints.reduce((sum, p) => sum + p.x, 0) / realPoints.length;
            const avgY =
              realPoints.reduce((sum, p) => sum + p.y, 0) / realPoints.length;

            // 算原本形狀的 Y 高度
            const yValues = realPoints.map((p) => p.y);
            const minY = Math.min(...yValues);
            const maxY = Math.max(...yValues);
            const height = maxY - minY;

            // 取得圖表 Y 軸顯示範圍與最大高度限制 (40% 螢幕高度)
            const yRange = chart.yAxis[0].max - chart.yAxis[0].min;
            const maxHeightRatio = 0.4; // 最大可佔比例
            const maxHeight = yRange * maxHeightRatio;

            // 計算縮放比例
            let scaleY = 1;
            if (height > maxHeight) {
              scaleY = maxHeight / height;
            }

            // 以圖表中心點為基準偏移
            const offsetX = centerX - avgX;

            // Y 軸用 centerY 重新計算（這裡centerY要先定義）
            const centerY = (chart.yAxis[0].min + chart.yAxis[0].max) / 2;

            // 更新每個點：縮放 Y + 平移 X,Y
            shape.points = shape.points.map((p) => {
              if (p.x !== undefined && p.y !== undefined) {
                return {
                  ...p,
                  x: p.x + offsetX,
                  y: centerY + (p.y - avgY) * scaleY,
                };
              }
              return p;
            });
          } else if (shape?.point) {
            // 單點 shape（如 circle）
            shape.point.x = centerX;
            shape.point.y = centerY;
          } else if (label?.point) {
            // label
            label.point.x = centerX;
            label.point.y = centerY;
          } else {
            console.warn("無法辨識 annotation 結構");
          }

          chart.addAnnotation(copiedAnnotation);
        })
        .catch((error) => {
          console.error("載入 shape.json 失敗:", error);
        });
    },
    switchInterval(type) {
      if (this.interval === type) return;
      let _this = this;
      this.interval = type;
      $.ajax({
        url: `ohlc/${this.symbolName}/${this.interval}`,
        type: "get",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          _this.ohlc = [];
          _this.volume = [];
          response.forEach((item) => {
            const date = item.date;
            const open = item.open;
            const high = item.high;
            const low = item.low;
            const close = item.close;
            const symbol = item.symbol;
            const interval = item.interval;
            const id = item.id + "_" + date;

            const timestamp = new Date(date).getTime();

            _this.ohlc.push([timestamp, open, high, low, close]);

            // 存下 id 對應 timestamp
            _this.ohlcIdMap.set(timestamp, id);

            _this.volume.push({
              x: timestamp,
              y: item.volume,
              color: close > open ? "red" : "green",
            });
          });

          _this.symbolTitle = response[0].symbolName;

          const chart = _this.initChart(_this.ohlc, _this.volume);
          window.chart = chart;

          const smaList = _this.userSettings?.smaList || _this.DEFAULT_SMA_LIST;

          smaList.forEach((ma) => {
            if (ma.checked) {
              const period = parseInt(ma.id.replace("sma", ""), 10);
              _this.addSMA(period, ma.color);
            }
          });

          // 顯示最後一筆股價的資料
          const last = _this.ohlc[_this.ohlc.length - 1];
          const [ts, open, high, low, close] = last;
          const lastVolumeObj = _this.volume.find((v) => v.x === ts) || {
            y: 0,
          };
          const volumeVal = lastVolumeObj.y;

          const change = close - open;
          const changePercent = (change / open) * 100;
          const isRise = change >= 0;

          _this.KDate = Highcharts.dateFormat("%Y-%m-%d", ts);
          _this.KOpen = open;
          _this.KHigh = high;
          _this.KLow = low;
          _this.KClose = close;
          _this.KVolume = volumeVal;
          _this.KChange = change;
          _this.KChangePercent = changePercent;
          _this.KIsRise = isRise;
        },
      });
    },
    getDateRange() {
      const extremes = chart.xAxis[0].getExtremes();
      const min = extremes.min; // 起始時間（毫秒 timestamp）
      const max = extremes.max; // 結束時間（毫秒 timestamp）
      let dateRange = {
        start: min,
        end: max,
      };
      return dateRange;
    },

    // note
    saveNote() {
      let _this = this;
      let data = {};
      data.id = this.noteId;
      data.layoutId = this.layoutId;
      data.content = window.editor.getData();
      $.ajax({
        url: "note/save",
        type: "post",
        dataType: "json",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
          console.log(response);
          _this.noteId = response.id;
          _this.unsavedLabel = false;

          const index = _this.notes.findIndex(note => note.id === response.id);
          if (index !== -1) {
            _this.notes[index] = response;
          } else {
            _this.notes.push(response);
          }
        },
      }).catch((xhr) => {
        console.log(xhr);
        console.log("系統異常，請稍後再試");
      });
    },
    formatTimestamp(timestamp) {
      if (!timestamp) return "";
      const date = new Date(timestamp);
      return `${date.getFullYear()}/${(date.getMonth() + 1)
        .toString()
        .padStart(2, "0")}/${date.getDate().toString().padStart(2, "0")} ${date
        .getHours()
        .toString()
        .padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
    },
  };
}

var linePickr = Pickr.create({
  el: ".line-color-picker",
  theme: "classic",
  autoReposition: true,
  swatches: swatches,
  components: {
    preview: true,
    opacity: false,
    hue: false,
    interaction: {
      hex: false,
      rgba: false,
      hsla: false,
      hsva: false,
      cmyk: false,
      input: false,
      clear: false,
      save: false,
    },
  },
});
linePickr.on("change", (color) => {
  const rgba = color.toRGBA().toString();
  console.log("Line Color 選色:", rgba);
  // 監聽並附值到 default modal (line)
  $('input[highcharts-data-name="typeOptions.line.stroke"]').val(rgba);
  // 監聽並附值到 default modal (shape)
  $('input[highcharts-data-name="shapes.0.stroke"]').val(rgba);
  // 監聽並附值到 default modal (label)
  $('input[highcharts-data-name="labelOptions.style.color"]').val(rgba);
  linePickr.applyColor(true);
  linePickr.hide();
});

var fillPickr = Pickr.create({
  el: ".fill-color-picker",
  theme: "classic",
  autoReposition: true,
  swatches: swatches,
  components: {
    preview: true,
    opacity: false,
    hue: false,
    interaction: {
      hex: false,
      rgba: false,
      hsla: false,
      hsva: false,
      cmyk: false,
      input: false,
      clear: false,
      save: false,
    },
  },
});
fillPickr.on("change", (color) => {
  const rgba = color.toRGBA().toString();
  console.log("fill Color 選色:", rgba);
  // 監聽並附值到 default modal (line)
  $('input[highcharts-data-name="typeOptions.line.fill"]').val(rgba);
  // 監聽並附值到 default modal (shape)
  $('input[highcharts-data-name="shapes.0.fill"]').val(rgba);
  // 監聽並附值到 default modal (label)
  $('input[highcharts-data-name="labelOptions.backgroundColor"]').val(rgba);
  fillPickr.applyColor(true);
  fillPickr.hide();
});

// ==== 設定自定義 Modal ==== //
// 1.隱藏 default modal、觸發自定義 modal 開啟
$(document).on("click", ".highcharts-annotation-edit-button", function () {
  // 先清空自定義 modal 的所有 input value
  $("#width").val("");
  $("#text").val("");
  $("#textSize").val("");
  $("#label0").val("");
  $("#label1").val("");
  $("#label2").val("");
  $("#label3").val("");
  $("#label4").val("");
  $("#label5").val("");

  // 取得線條顏色並附值
  const strokeColor = $(
    'input[highcharts-data-name="typeOptions.line.stroke"]'
  ).val();
  if (strokeColor != undefined) {
    console.log("線條顏色 strokeColor:", strokeColor);
    linePickr.setColor(strokeColor);
  }

  // 取得線條寬度並附值
  const strokeWidth = $(
    'input[highcharts-data-name="typeOptions.line.strokeWidth"]'
  ).val();
  if (strokeWidth != undefined) {
    console.log("線條寬度 strokeWidth:", strokeWidth);
    $("#width").val(strokeWidth);
  }

  // 取得 shape 的線條顏色並附值
  const shapeColor = $('input[highcharts-data-name="shapes.0.stroke"]').val();
  if (shapeColor != undefined) {
    console.log("Shape 線條顏色 shapeColor:", shapeColor);
    linePickr.setColor(shapeColor);
  }

  // 取得 shape 的線條寬度並附值
  const shapeStrokeWidth = $(
    'input[highcharts-data-name="shapes.0.strokeWidth"]'
  ).val();
  if (shapeStrokeWidth != undefined) {
    console.log("Shape 線條寬度 shapeStrokeWidth:", shapeStrokeWidth);
    $("#width").val(shapeStrokeWidth);
  }

  // 取得 shape 的填滿顏色並附值
  const shapeFillColor = $('input[highcharts-data-name="shapes.0.fill"]').val();
  if (shapeFillColor != undefined) {
    console.log("Shape 填滿顏色 shapeFillColor:", shapeFillColor);
    if (shapeFillColor == "none") {
      fillPickr.setColor("rgba(0, 0, 0, 0)");
    } else {
      fillPickr.setColor(shapeFillColor);
    }
  }

  // 取得文字內容並附值
  const textContent = $(
    'input[highcharts-data-name="labelOptions.format"]'
  ).val();
  if (textContent != undefined) {
    console.log("文字內容 textContent:", textContent);
    $("#text").val(textContent);
  }

  // 取得文字區塊背景顏色並附值
  const textBackgroundColor = $(
    'input[highcharts-data-name="labelOptions.backgroundColor"]'
  ).val();
  if (textBackgroundColor != undefined) {
    console.log("文字背景顏色 textBackgroundColor:", textBackgroundColor);
    fillPickr.setColor(textBackgroundColor);
  }

  // 取得文字大小並附值
  const textSize = $(
    'input[highcharts-data-name="labelOptions.style.fontSize"]'
  ).val();
  if (textSize != undefined) {
    console.log("文字大小 textSize:", textSize);
    const numberOnly = textSize.match(/\d+(\.\d+)?/);
    console.log(numberOnly && numberOnly[0]);
    if (numberOnly) {
      $("#textSize").val(numberOnly[0]);
    }
  }

  // 取得文字顏色並附值
  const textColor = $(
    'input[highcharts-data-name="labelOptions.style.color"]'
  ).val();
  if (textColor != undefined) {
    console.log("文字顏色 textColor:", textColor);
    linePickr.setColor(textColor);
  }

  // 取得 label 文字
  const label0 = $('input[highcharts-data-name="typeOptions.labels.0"]').val();
  const label1 = $('input[highcharts-data-name="typeOptions.labels.1"]').val();
  const label2 = $('input[highcharts-data-name="typeOptions.labels.2"]').val();
  const label3 = $('input[highcharts-data-name="typeOptions.labels.3"]').val();
  const label4 = $('input[highcharts-data-name="typeOptions.labels.4"]').val();
  const label5 = $('input[highcharts-data-name="typeOptions.labels.5"]').val();
  if (label0 != undefined) $("#label0").val(label0);
  if (label1 != undefined) $("#label1").val(label1);
  if (label2 != undefined) $("#label2").val(label2);
  if (label3 != undefined) $("#label3").val(label3);
  if (label4 != undefined) $("#label4").val(label4);
  if (label5 != undefined) $("#label5").val(label5);

  // 關閉預設 modal，並開啟自定義 modal
  const $closeBtn = $(".highcharts-popup-close");
  if ($closeBtn.length) {
    $closeBtn.trigger("click");
  } else {
    $(".highcharts-popup").hide(); // 備援隱藏
  }
  $("#styleModal").modal("show");
});

$(document).on("mouseenter", ".highcharts-popup", function () {
  $(".color-input-area").addClass("hidden");
  $(".width-input-area").addClass("hidden");
  $(".fill-input-area").addClass("hidden");
  $(".text-input-area").addClass("hidden");
  $(".label-input-area").addClass("hidden");
  // 滑鼠移入 popup 時，記錄當前的文字
  const text = $(this).find("p.highcharts-annotation-label").text().trim();
  if (
    text == "Segment" ||
    text == "Arrow segment" ||
    text == "Crooked 3 line" ||
    text == "Horizontal line"
  ) {
    $(".color-input-area").removeClass("hidden");
    $(".width-input-area").removeClass("hidden");
    $(".fill-input-area").removeClass("hidden");
  } else if (text == "Circle" || text == "Rectangle") {
    $(".color-input-area").removeClass("hidden");
    $(".width-input-area").removeClass("hidden");
    $(".fill-input-area").removeClass("hidden");
  } else if (text == "Label" || text == "Vertical label") {
    $(".color-input-area").removeClass("hidden");
    $(".fill-input-area").removeClass("hidden");
    $(".text-input-area").removeClass("hidden");
  } else if (text == "Elliott 3 line" || text == "Elliott 5 line") {
    $(".color-input-area").removeClass("hidden");
    $(".width-input-area").removeClass("hidden");
    $(".label-input-area").removeClass("hidden");
  }
});

// 輸入值附值給預設 modal
$(document).on("input", "#width", function () {
  const lineWidth = $(this).val();
  $('input[highcharts-data-name="typeOptions.line.strokeWidth"]').val(lineWidth);
});
$(document).on("input", "#width", function () {
  const lineWidth = $(this).val();
  $('input[highcharts-data-name="shapes.0.strokeWidth"]').val(lineWidth);
});
$(document).on("input", "#text", function () {
  const textValue = $(this).val();
  $('input[highcharts-data-name="labelOptions.format"]').val(textValue);
});
$(document).on("input", "#textSize", function () {
  const fontSize = $(this).val();
  $('input[highcharts-data-name="labelOptions.style.fontSize"]').val(fontSize);
});
$(document).on("input", "#label0", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.0"]').val(color);
});
$(document).on("input", "#label1", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.1"]').val(color);
});
$(document).on("input", "#label2", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.2"]').val(color);
});
$(document).on("input", "#label3", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.3"]').val(color);
});
$(document).on("input", "#label4", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.4"]').val(color);
});
$(document).on("input", "#label5", function () {
  const color = $(this).val();
  $('input[highcharts-data-name="typeOptions.labels.5"]').val(color);
});

// 模擬觸發預設 modal 的 save button
$(document).on("click", "#saveStyleBtn", function () {
  const $btn = $(".highcharts-popup-bottom-row").find("button");
  if ($btn.length > 0) {
    $btn[0].click();
  } else {
    console.warn("Save 按鈕尚未出現");
  }

  $(".color-input-area").addClass("hidden");
  $(".fill-input-area").addClass("hidden");
  $(".text-input-area").addClass("hidden");
  $(".width-input-area").addClass("hidden");
  $(".label-input-area").addClass("hidden");

  $("#styleModal").modal("hide");
});
