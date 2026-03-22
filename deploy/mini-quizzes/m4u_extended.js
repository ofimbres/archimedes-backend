/**
 * Miniquiz submit: POST assignment completion to Archimedes API.
 * Reads from the page URL (query + hash):
 *   - assignment_id, archimedes_api_base, student_id (query)
 *   - access_token or id_token (URL hash, preferred over query for the token)
 * Optional: window.M4UConfig.archimedes.accessToken or M4UConfig.webhook.headers.Authorization
 * Ensure CORS allows this miniquiz origin on the API (CORS_ORIGINS).
 * Sync with archimedes-infrastructure data/miniquizzes/html_files/m4u_extended.js when publishing.
 */
function parseScoreForArchimedes(gradeAttr) {
   if (gradeAttr == null || gradeAttr === '') return null;
   var s = String(gradeAttr).trim();
   var n = parseFloat(s.replace(/[^0-9.\-]/g, ''));
   if (!Number.isFinite(n)) return null;
   if (n > 100) return 100;
   if (n < 0) return 0;
   return n;
}

function getArchimedesBearerToken() {
   try {
      var arch = window.M4UConfig && window.M4UConfig.archimedes;
      if (arch && arch.accessToken) return String(arch.accessToken);
      var wh = window.M4UConfig && window.M4UConfig.webhook && window.M4UConfig.webhook.headers;
      if (wh && wh.Authorization && typeof wh.Authorization === 'string') {
         var a = wh.Authorization;
         if (a.indexOf('Bearer ') === 0) return a.slice(7).trim();
         return a.trim();
      }
   } catch (e) { /* ignore */ }
   if (window.location.hash.length > 1) {
      var hp = new URLSearchParams(window.location.hash.substring(1));
      var t = hp.get('access_token') || hp.get('id_token');
      if (t) return t;
   }
   var qp = new URLSearchParams(window.location.search);
   return qp.get('access_token') || qp.get('id_token');
}

function submitArchimedesAssignmentCompletion(grade) {
   var params = new URLSearchParams(window.location.search);
   var assignmentId = params.get('assignment_id');
   var apiBase = params.get('archimedes_api_base');
   var studentId = params.get('student_id');
   var token = getArchimedesBearerToken();
   if (!assignmentId || !apiBase || !studentId || !token) {
      console.warn(
         'Archimedes completion skipped: need assignment_id, archimedes_api_base, student_id query params and a Bearer token (hash #access_token=... or M4UConfig.archimedes.accessToken)'
      );
      return Promise.resolve();
   }
   var score = parseScoreForArchimedes(grade);
   var base = String(apiBase).replace(/\/$/, '');
   var url = base + '/api/v1/assignments/' + assignmentId + '/completions';
   var body = { student_id: studentId };
   if (score !== null) body.score = score;
   return fetch(url, {
      method: 'POST',
      mode: 'cors',
      headers: {
         'Content-Type': 'application/json',
         Accept: 'application/json',
         Authorization: 'Bearer ' + token,
      },
      body: JSON.stringify(body),
   }).then(function (res) {
      if (!res.ok) {
         return res.text().then(function (txt) {
            console.warn('Archimedes completion failed', res.status, txt);
         });
      }
   }).catch(function (err) {
      console.warn('Archimedes completion error', err);
   });
}

function submitForm() {
   const studentGradeCell = document.querySelectorAll('[data-grade-field]')[0];
   var grade = studentGradeCell.getAttribute('data-cval');
   var worksheetCopy = createStaticForm(true);

   const message = {
      grade: grade,
      worksheetCopy: worksheetCopy,
   };

   window.parent.postMessage(message, '*');
   submitArchimedesAssignmentCompletion(grade).finally(function () {
      document.write('Submitting results. Do not refresh the page');
   });

   return false;
}

function updateCellFields(event) {
   const studentNameElement = document.querySelectorAll('[data-name-field]')[0];
   const studentIdElement = document.querySelectorAll('[data-id-field]')[0];

   let message = event.data;

   studentNameElement.innerHTML = message.studentName;
   studentIdElement.innerHTML = generateNumericId(message.studentId);

   studentIdElement.setAttribute('data-cval', generateNumericId(message.studentId));
   calculate(studentIdElement.id);
}

function init() {
   window.addEventListener('message', updateCellFields, false);
}

function generateNumericId(studentId) {
   const hash = hashCode(studentId).toString();
   return hash.substring(hash.length - 5);
}

function hashCode(str) {
   var hash = 0,
      i,
      chr;
   if (str.length === 0) return hash;
   for (i = 0; i < str.length; i++) {
      chr = str.charCodeAt(i);
      hash = (hash << 5) - hash + chr;
      hash |= 0;
   }
   return hash;
}

init();
