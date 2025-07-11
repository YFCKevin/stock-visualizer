document.addEventListener('DOMContentLoaded', function () {
  // 日期選擇器 tempus-dominus
  const publishedDateElement = document.getElementById('newsPublishedAt');
  console.log(publishedDateElement)
  const publishedDate = new tempusDominus.TempusDominus(publishedDateElement, {
    localization: {
      format: 'yyyy-MM-dd'
    },
    useCurrent: false,
  });
//  const endDate = new tempusDominus.TempusDominus(document.getElementById('endDate'),{
//    localization: {
//      format: 'yyyy-MM-dd'
//    },
//  });
//  startDateElement.addEventListener(tempusDominus.Namespace.events.change, (e) => {
//    endDate.updateOptions({
//      restrictions: {
//        minDate: e.detail.date,
//      },
//    });
//  });
//  const recordSubscription = endDate.subscribe(tempusDominus.Namespace.events.change, (e) => {
//    startDate.updateOptions({
//      restrictions: {
//        maxDate: e.date,
//      },
//    });
//  });
});