// tilt.js
// Apply a subtle tilt to images inside the horizontal scroller when the user scrolls left/right.
// Behavior:
// - Listens to scroll events on the .btn-wrap container
// - Computes horizontal scroll velocity (delta) and applies a rotateZ + small rotateX for depth
// - Applies a short easing so images return to neutral when scrolling stops

(function () {
    const container = document.querySelector('.btn-wrap');
    if (!container) return;

    let lastScrollLeft = container.scrollLeft;
    let lastTime = performance.now();
    let rafId = null;

    // Parameters you can tune
    const maxTilt = 8; // degrees maximum rotation
    const decayMs = 300; // ms to decay back to 0

    // Track current tilt applied so we can smoothly decay
    let currentTilt = 0;
    let targetTilt = 0;

    function applyTilt(angle) {
        const imgs = container.querySelectorAll('.img-area img');
        imgs.forEach(img => {
            // Use combined rotateZ and tiny rotateX to give a 3D feel
            img.style.transform = `rotateZ(${angle}deg) translateZ(0) rotateX(${Math.abs(angle) * 0.08}deg)`;
        });
    }

    // scroll-stop timer: when no scroll events occur for this many ms, we reset immediately
    const SCROLL_STOP_MS = 60;
    let scrollStopTimer = null;

    function clearScrollStopTimer() {
        if (scrollStopTimer) {
            clearTimeout(scrollStopTimer);
            scrollStopTimer = null;
        }
    }

    function onScroll() {
        const now = performance.now();
        const scrollLeft = container.scrollLeft;
        const dt = Math.max(1, now - lastTime);
        const dx = scrollLeft - lastScrollLeft;

        // velocity px/ms
        const v = dx / dt;

        // map velocity to tilt angle
        const rawTilt = Math.max(-maxTilt, Math.min(maxTilt, v * 200));
        targetTilt = rawTilt;

        lastScrollLeft = scrollLeft;
        lastTime = now;

        // reset scroll-stop timer: if no scroll events for SCROLL_STOP_MS, snap back to neutral
        clearScrollStopTimer();
        scrollStopTimer = setTimeout(() => {
            // immediately clear tilt
            targetTilt = 0;
            currentTilt = 0;
            applyTilt(0);
            if (rafId) { cancelAnimationFrame(rafId); rafId = null; }
        }, SCROLL_STOP_MS);

        // start a raf loop if not running
        if (!rafId) rafId = requestAnimationFrame(rafStep);
    }

    function rafStep() {
        // simple easing toward targetTilt
        currentTilt += (targetTilt - currentTilt) * 0.28;

        // when target is near zero, also decay it slowly
        if (Math.abs(targetTilt) < 0.01) {
            currentTilt *= 0.82;
        }

        applyTilt(currentTilt);

        // if both target and current are near zero, stop the loop
        if (Math.abs(currentTilt) < 0.01 && Math.abs(targetTilt) < 0.01) {
            applyTilt(0);
            rafId = null;
            return;
        }

        rafId = requestAnimationFrame(rafStep);
    }

    // Also add pointer/touch move support for smoother response on mobile when user swipes inside the container
    let isPointerDown = false;
    let pointerStartX = 0;
    let startScroll = 0;

    container.addEventListener('scroll', onScroll, { passive: true });

    container.addEventListener('pointerdown', (e) => {
        isPointerDown = true;
        pointerStartX = e.clientX;
        startScroll = container.scrollLeft;
    });

    container.addEventListener('pointermove', (e) => {
        if (!isPointerDown) return;
        const dx = pointerStartX - e.clientX;
        // estimate tilt from drag direction
        targetTilt = Math.max(-maxTilt, Math.min(maxTilt, (dx / 20)));
        if (!rafId) rafId = requestAnimationFrame(rafStep);
    });

    ['pointerup', 'pointercancel', 'pointerleave'].forEach(ev => {
        container.addEventListener(ev, () => {
            isPointerDown = false;
            // immediately reset transforms when pointer interaction ends
            clearScrollStopTimer();
            targetTilt = 0;
            currentTilt = 0;
            applyTilt(0);
            if (rafId) { cancelAnimationFrame(rafId); rafId = null; }
        });
    });

    // When page becomes hidden, clear transforms
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            applyTilt(0);
        }
    });

    // initial tilt reset
    applyTilt(0);
})();
