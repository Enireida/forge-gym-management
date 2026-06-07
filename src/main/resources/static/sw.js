// Forge Gym Service Worker — v2.0
// Strategy: cache static assets (CSS/JS/images) only.
// Navigation requests (HTML pages) are NEVER intercepted —
// the browser always goes directly to the server for them.
// This prevents the "offline" page from showing on normal reloads.

const CACHE_NAME = 'forge-v2';

// Install: pre-cache only the stylesheet (guaranteed 200)
self.addEventListener('install', function(event) {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) { return cache.add('/css/app.css'); })
      .then(function() { return self.skipWaiting(); })
  );
});

// Activate: remove all old cache versions
self.addEventListener('activate', function(event) {
  event.waitUntil(
    caches.keys().then(function(keys) {
      return Promise.all(
        keys.filter(function(k) { return k !== CACHE_NAME; })
            .map(function(k) { return caches.delete(k); })
      );
    }).then(function() { return self.clients.claim(); })
  );
});

// Fetch: handle only static asset requests (css / js / img / fonts).
// Navigation requests (mode === 'navigate') are NOT intercepted at all —
// returning without calling event.respondWith() lets the browser handle
// them normally, so a server restart or login redirect works correctly.
self.addEventListener('fetch', function(event) {
  if (event.request.method !== 'GET') return;
  if (!event.request.url.startsWith('http')) return;

  // ✅ Let the browser handle ALL HTML navigation normally
  if (event.request.mode === 'navigate') return;

  var url = new URL(event.request.url);
  var isStaticAsset = url.pathname.startsWith('/css/')
                   || url.pathname.startsWith('/js/')
                   || url.pathname.startsWith('/img/')
                   || url.pathname.startsWith('/fonts/');

  if (!isStaticAsset) return; // ignore anything else (API calls, etc.)

  // Stale-while-revalidate for static assets
  event.respondWith(
    caches.match(event.request).then(function(cached) {
      var networkFetch = fetch(event.request).then(function(response) {
        if (response && response.status === 200) {
          var clone = response.clone();
          caches.open(CACHE_NAME).then(function(cache) {
            cache.put(event.request, clone);
          });
        }
        return response;
      }).catch(function() {
        // Network failed for a static asset — return cached version if any
        return cached;
      });
      // Return cache immediately (fast), update in background
      return cached || networkFetch;
    })
  );
});
