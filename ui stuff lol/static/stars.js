// stars.js — lightweight starfield generator
(function(){
    const STAR_COUNT = 90; // tune density
    const containerId = 'starfield';

    function rand(min, max){ return Math.random() * (max - min) + min; }

    function createStar(i, w, h){
        const el = document.createElement('div');
        el.className = 'star';
        const size = rand(1, 4); // px
        el.style.width = `${size}px`;
        el.style.height = `${size}px`;
        el.dataset.vx = rand(-0.02, 0.02); // horizontal velocity px per ms
        el.dataset.vy = rand(0.004, 0.03); // vertical drift
        // random position
        el.style.left = `${rand(0, w)}px`;
        el.style.top = `${rand(0, h)}px`;
        // twinkle
        el.style.animation = `star-twinkle ${rand(2000,6000)}ms ease-in-out ${rand(0,2000)}ms infinite`;
        return el;
    }

    function init(){
        const container = document.getElementById(containerId) || document.createElement('div');
        container.id = containerId;
        document.body.appendChild(container);

        const rect = { w: window.innerWidth, h: window.innerHeight };

        const stars = [];
        for(let i=0;i<STAR_COUNT;i++){
            const s = createStar(i, rect.w, rect.h);
            container.appendChild(s);
            stars.push(s);
        }

        let last = performance.now();
        function step(now){
            const dt = now - last; last = now;
            for(const s of stars){
                let x = parseFloat(s.style.left);
                let y = parseFloat(s.style.top);
                const vx = parseFloat(s.dataset.vx) * dt * 0.5; // scale down so movement is very slow
                const vy = parseFloat(s.dataset.vy) * dt * 0.5;
                x += vx;
                y += vy;
                // recycle if out of bounds
                if (x < -20) x = rect.w + 10;
                if (x > rect.w + 20) x = -10;
                if (y > rect.h + 20) y = -10;
                s.style.left = `${x}px`;
                s.style.top = `${y}px`;
            }
            requestAnimationFrame(step);
        }

        window.addEventListener('resize', ()=>{
            rect.w = window.innerWidth; rect.h = window.innerHeight;
        });

        requestAnimationFrame(step);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
