// Temporary: a DOM-based XSS on purpose, to check that CodeQL reports it.
// The next commit removes this file.
export function renderFromUrl(): void {
  document.body.innerHTML = decodeURIComponent(window.location.hash.slice(1));
}
