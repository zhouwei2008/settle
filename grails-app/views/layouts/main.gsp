<!DOCTYPE html>
<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <meta charset="utf-8">
        <link rel="stylesheet" href="${resource(dir: 'css/cupertino', file: 'jquery-ui-1.8.20.custom.css')}"/>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

        <g:layoutHead />
          <g:javascript library="jquery-1.7.2.min"/>
          <g:javascript library="jquery-ui-1.8.20.custom.min"/>
        <g:javascript library="jquery.ui.datepicker-zh-CN"/>
        <g:javascript library="application" />
    </head>
    <body>
 <script type="text/javascript">
  $(function() {
    $(".back_btn").click(function() {
      window.history.back();
    })
  })
</script>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
        </div>
        <div id="grailsLogo"><a href="http://grails.org"><img src="${resource(dir:'images',file:'grails_logo.png')}" alt="Grails" border="0" /></a></div>
        <g:layoutBody />
    </body>
</html>