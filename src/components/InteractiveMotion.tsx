import React, { useEffect, useRef, useState } from 'react';

export function usePrefersReducedMotion(): boolean {
  const [reduced, setReduced] = useState<boolean>(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return false;
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)');
    const onChange = () => setReduced(mq.matches);
    mq.addEventListener?.('change', onChange);
    return () => mq.removeEventListener?.('change', onChange);
  }, []);

  return reduced;
}

interface BridgeNode {
  x: number;
  y: number;
  z: number;
  vx: number;
  vy: number;
  vz: number;
  size: number;
  accent: boolean;
}

interface RippleWave {
  x: number;
  y: number;
  radius: number;
  maxRadius: number;
  alpha: number;
}

export const LivingBridgeCanvas: React.FC<{
  className?: string;
  density?: 'hero' | 'ambient';
}> = ({ className = '', density = 'hero' }) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const reducedMotion = usePrefersReducedMotion();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let rafId = 0;
    let width = 0;
    let height = 0;
    const isNarrow = window.innerWidth < 768;
    const nodeCount = density === 'hero' ? (isNarrow ? 26 : 44) : isNarrow ? 16 : 28;

    const pointer = { x: 0.5, y: 0.45, targetX: 0.5, targetY: 0.45, active: false };
    const ripples: RippleWave[] = [];

    const nodes: BridgeNode[] = Array.from({ length: nodeCount }, (_, idx) => ({
      x: (Math.random() - 0.5) * 900,
      y: (Math.random() - 0.5) * 520,
      z: Math.random() * 420 + 60,
      vx: (Math.random() - 0.5) * 0.28,
      vy: (Math.random() - 0.5) * 0.22,
      vz: (Math.random() - 0.5) * 0.35,
      size: idx % 5 === 0 ? 2.6 : 1.6,
      accent: idx % 4 === 0,
    }));

    const resize = () => {
      const rect = canvas.getBoundingClientRect();
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      width = Math.max(1, rect.width);
      height = Math.max(1, rect.height);
      canvas.width = Math.round(width * dpr);
      canvas.height = Math.round(height * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    };

    resize();
    window.addEventListener('resize', resize, { passive: true });

    const onPointerMove = (e: PointerEvent) => {
      const rect = canvas.getBoundingClientRect();
      if (rect.width <= 0 || rect.height <= 0) return;
      const nx = (e.clientX - rect.left) / rect.width;
      const ny = (e.clientY - rect.top) / rect.height;
      if (nx >= -0.1 && nx <= 1.1 && ny >= -0.1 && ny <= 1.1) {
        pointer.targetX = Math.max(0, Math.min(1, nx));
        pointer.targetY = Math.max(0, Math.min(1, ny));
        pointer.active = true;
      }
    };

    const onPointerDown = (e: PointerEvent) => {
      const rect = canvas.getBoundingClientRect();
      const rx = e.clientX - rect.left;
      const ry = e.clientY - rect.top;
      if (rx >= 0 && rx <= rect.width && ry >= 0 && ry <= rect.height) {
        ripples.push({
          x: rx,
          y: ry,
          radius: 8,
          maxRadius: Math.min(180, Math.max(width, height) * 0.28),
          alpha: 0.45,
        });
      }
    };

    window.addEventListener('pointermove', onPointerMove, { passive: true });
    window.addEventListener('pointerdown', onPointerDown, { passive: true });

    const drawFrame = (animate: boolean) => {
      ctx.clearRect(0, 0, width, height);
      const isDark = document.documentElement.classList.contains('dark');
      const scrollParallax = Math.min(120, window.scrollY * 0.14);

      pointer.x += (pointer.targetX - pointer.x) * 0.06;
      pointer.y += (pointer.targetY - pointer.y) * 0.06;

      const camOffsetX = (pointer.x - 0.5) * 90;
      const camOffsetY = (pointer.y - 0.5) * 56 - scrollParallax * 0.35;
      const fov = 360;

      const projected: Array<{ sx: number; sy: number; scale: number; depthAlpha: number; node: BridgeNode }> = [];

      for (const node of nodes) {
        if (animate) {
          node.x += node.vx;
          node.y += node.vy;
          node.z += node.vz;
          if (node.x > 480) node.x = -480;
          if (node.x < -480) node.x = 480;
          if (node.y > 280) node.y = -280;
          if (node.y < -280) node.y = 280;
          if (node.z > 480) node.z = 60;
          if (node.z < 60) node.z = 480;
        }

        const scale = fov / (fov + node.z);
        const sx = width * 0.5 + (node.x - camOffsetX) * scale;
        const sy = height * 0.5 + (node.y - camOffsetY) * scale;
        const depthAlpha = Math.max(0.12, Math.min(0.85, 1 - node.z / 540));

        if (sx > -40 && sx < width + 40 && sy > -40 && sy < height + 40) {
          projected.push({ sx, sy, scale, depthAlpha, node });
        }
      }

      // Draw constellation bridge connections
      const maxLinkDist = isNarrow ? 115 : 155;
      for (let i = 0; i < projected.length; i++) {
        const a = projected[i];
        for (let j = i + 1; j < projected.length; j++) {
          const b = projected[j];
          const dx = a.sx - b.sx;
          const dy = a.sy - b.sy;
          const dist = Math.hypot(dx, dy);
          if (dist < maxLinkDist) {
            const strength = (1 - dist / maxLinkDist) * Math.min(a.depthAlpha, b.depthAlpha);
            ctx.strokeStyle = isDark
              ? `rgba(126, 232, 245, ${(strength * 0.28).toFixed(3)})`
              : `rgba(0, 140, 174, ${(strength * 0.22).toFixed(3)})`;
            ctx.lineWidth = Math.max(0.5, strength * 1.4);
            ctx.beginPath();
            ctx.moveTo(a.sx, a.sy);
            ctx.lineTo(b.sx, b.sy);
            ctx.stroke();
          }
        }
      }

      // Draw nodes with gentle blue glow halo
      for (const p of projected) {
        const r = p.node.size * p.scale * 1.35;
        if (p.node.accent) {
          ctx.fillStyle = isDark
            ? `rgba(0, 180, 219, ${(p.depthAlpha * 0.22).toFixed(3)})`
            : `rgba(0, 180, 219, ${(p.depthAlpha * 0.16).toFixed(3)})`;
          ctx.beginPath();
          ctx.arc(p.sx, p.sy, r * 3.4, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.fillStyle = p.node.accent
          ? isDark
            ? `rgba(126, 232, 245, ${p.depthAlpha.toFixed(3)})`
            : `rgba(0, 140, 174, ${p.depthAlpha.toFixed(3)})`
          : isDark
            ? `rgba(0, 180, 219, ${(p.depthAlpha * 0.72).toFixed(3)})`
            : `rgba(11, 31, 56, ${(p.depthAlpha * 0.36).toFixed(3)})`;
        ctx.beginPath();
        ctx.arc(p.sx, p.sy, r, 0, Math.PI * 2);
        ctx.fill();
      }

      // Draw interactive tap / click ripples
      for (let i = ripples.length - 1; i >= 0; i--) {
        const rip = ripples[i];
        if (animate) {
          rip.radius += 3.2;
          rip.alpha *= 0.94;
        }
        ctx.strokeStyle = isDark
          ? `rgba(126, 232, 245, ${rip.alpha.toFixed(3)})`
          : `rgba(0, 180, 219, ${rip.alpha.toFixed(3)})`;
        ctx.lineWidth = 1.25;
        ctx.beginPath();
        ctx.arc(rip.x, rip.y, rip.radius, 0, Math.PI * 2);
        ctx.stroke();

        if (rip.alpha < 0.02 || rip.radius > rip.maxRadius) {
          ripples.splice(i, 1);
        }
      }
    };

    if (reducedMotion) {
      drawFrame(false);
      return () => {
        window.removeEventListener('resize', resize);
        window.removeEventListener('pointermove', onPointerMove);
        window.removeEventListener('pointerdown', onPointerDown);
      };
    }

    let running = true;
    const loop = () => {
      if (!running) return;
      if (!document.hidden) {
        drawFrame(true);
      }
      rafId = window.requestAnimationFrame(loop);
    };
    rafId = window.requestAnimationFrame(loop);

    return () => {
      running = false;
      window.cancelAnimationFrame(rafId);
      window.removeEventListener('resize', resize);
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('pointerdown', onPointerDown);
    };
  }, [density, reducedMotion]);

  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className={`pointer-events-none ${className}`}
    />
  );
};

export const SpatialCard: React.FC<{
  children: React.ReactNode;
  className?: string;
  tiltStrength?: number;
  highlight?: boolean;
  onClick?: () => void;
  id?: string;
}> = ({ children, className = '', tiltStrength = 5, highlight = false, onClick, id }) => {
  const cardRef = useRef<HTMLDivElement | null>(null);
  const reducedMotion = usePrefersReducedMotion();
  const [spot, setSpot] = useState({ x: 50, y: 50, active: false, rx: 0, ry: 0 });

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (reducedMotion || e.pointerType === 'touch') return;
    const el = cardRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const px = Math.max(0, Math.min(1, (e.clientX - rect.left) / Math.max(1, rect.width)));
    const py = Math.max(0, Math.min(1, (e.clientY - rect.top) / Math.max(1, rect.height)));
    const ry = (px - 0.5) * tiltStrength * 2;
    const rx = -(py - 0.5) * tiltStrength * 2;
    setSpot({ x: px * 100, y: py * 100, active: true, rx, ry });
  };

  const resetPointer = () => {
    setSpot((prev) => ({ ...prev, active: false, rx: 0, ry: 0 }));
  };

  const handleTouchActivate = (e: React.PointerEvent<HTMLDivElement>) => {
    if (reducedMotion) return;
    const el = cardRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const px = Math.max(0, Math.min(1, (e.clientX - rect.left) / Math.max(1, rect.width)));
    const py = Math.max(0, Math.min(1, (e.clientY - rect.top) / Math.max(1, rect.height)));
    setSpot({ x: px * 100, y: py * 100, active: true, rx: 0, ry: 0 });
  };

  const interactiveStyle: React.CSSProperties = reducedMotion
    ? {}
    : {
        transform: spot.active
          ? `perspective(950px) rotateX(${spot.rx.toFixed(2)}deg) rotateY(${spot.ry.toFixed(2)}deg) translate3d(0, -2px, 0)`
          : 'perspective(950px) rotateX(0deg) rotateY(0deg) translate3d(0, 0, 0)',
        transition: spot.active
          ? 'transform 90ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 220ms ease, border-color 200ms ease'
          : 'transform 340ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 240ms ease, border-color 200ms ease',
        willChange: 'transform',
      };

  return (
    <div
      id={id}
      ref={cardRef}
      onClick={onClick}
      onPointerMove={handlePointerMove}
      onPointerDown={handleTouchActivate}
      onPointerLeave={resetPointer}
      onPointerCancel={resetPointer}
      style={interactiveStyle}
      className={`group relative overflow-hidden active:scale-[0.992] ${
        highlight
          ? 'shadow-[0_20px_50px_-28px_rgba(0,180,219,0.35)] dark:shadow-[0_20px_50px_-28px_rgba(0,180,219,0.28)]'
          : 'hover:shadow-[0_18px_42px_-28px_rgba(11,31,56,0.22)] dark:hover:shadow-[0_18px_42px_-28px_rgba(0,180,219,0.24)]'
      } ${className}`}
    >
      {/* Interactive Cursor / Touch Specular Spotlight */}
      {!reducedMotion && (
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 transition-opacity duration-300"
          style={{
            opacity: spot.active ? 1 : 0,
            background: `radial-gradient(360px circle at ${spot.x}% ${spot.y}%, rgba(0, 180, 219, 0.13), rgba(126, 232, 245, 0.05) 42%, transparent 72%)`,
          }}
        />
      )}

      {/* Subtle Gaming HUD Corner Brackets on Active / Hover */}
      <span
        aria-hidden="true"
        className="pointer-events-none absolute top-2.5 start-2.5 w-2.5 h-2.5 border-t border-s border-[#00B4DB]/0 group-hover:border-[#00B4DB]/70 group-active:border-[#00B4DB]/80 transition-colors duration-200 rounded-tl-[3px]"
      />
      <span
        aria-hidden="true"
        className="pointer-events-none absolute bottom-2.5 end-2.5 w-2.5 h-2.5 border-b border-e border-[#00B4DB]/0 group-hover:border-[#00B4DB]/70 group-active:border-[#00B4DB]/80 transition-colors duration-200 rounded-br-[3px]"
      />

      <div className="relative z-10 h-full flex flex-col justify-between">{children}</div>
    </div>
  );
};

export const MotionReveal: React.FC<{
  children: React.ReactNode;
  delayMs?: number;
  className?: string;
  immediate?: boolean;
}> = ({ children, delayMs = 0, className = '', immediate = false }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const reducedMotion = usePrefersReducedMotion();
  const [visible, setVisible] = useState(immediate || reducedMotion);

  useEffect(() => {
    if (immediate || reducedMotion) {
      setVisible(true);
      return;
    }
    const el = ref.current;
    if (!el || typeof IntersectionObserver === 'undefined') {
      setVisible(true);
      return;
    }
    const obs = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            setVisible(true);
            obs.disconnect();
            break;
          }
        }
      },
      { threshold: 0.08, rootMargin: '0px 0px -24px 0px' },
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, [immediate, reducedMotion]);

  if (reducedMotion) {
    return <div className={className}>{children}</div>;
  }

  return (
    <div
      ref={ref}
      className={className}
      style={{
        opacity: visible ? 1 : 0,
        transform: visible ? 'translate3d(0, 0, 0)' : 'translate3d(0, 14px, 0)',
        transition: `opacity 480ms cubic-bezier(0.16, 1, 0.3, 1) ${delayMs}ms, transform 520ms cubic-bezier(0.16, 1, 0.3, 1) ${delayMs}ms`,
        willChange: 'opacity, transform',
      }}
    >
      {children}
    </div>
  );
};

export const AnimatedNumber: React.FC<{
  value: number;
  suffix?: string;
  durationMs?: number;
  className?: string;
}> = ({ value, suffix = '', durationMs = 680, className = '' }) => {
  const reducedMotion = usePrefersReducedMotion();
  const [display, setDisplay] = useState(value);
  const prevRef = useRef(value);

  useEffect(() => {
    if (reducedMotion) {
      setDisplay(value);
      prevRef.current = value;
      return;
    }
    const startVal = prevRef.current === value ? 0 : prevRef.current;
    const diff = value - startVal;
    const startTime = performance.now();
    let raf = 0;

    const tick = (now: number) => {
      const t = Math.min(1, (now - startTime) / durationMs);
      const eased = 1 - Math.pow(1 - t, 3);
      setDisplay(Math.round(startVal + diff * eased));
      if (t < 1) {
        raf = window.requestAnimationFrame(tick);
      } else {
        prevRef.current = value;
      }
    };
    raf = window.requestAnimationFrame(tick);
    return () => window.cancelAnimationFrame(raf);
  }, [value, durationMs, reducedMotion]);

  return (
    <span className={`tabular-nums ${className}`}>
      {display}
      {suffix}
    </span>
  );
};

export const AnimatedPlatformEmblem: React.FC<{
  kind: 'WEB' | 'MOBILE' | 'DESKTOP';
  active?: boolean;
}> = ({ kind, active = false }) => {
  return (
    <div
      className={`relative w-12 h-12 rounded-2xl border flex items-center justify-center shrink-0 transition-all duration-300 group-hover:scale-105 group-active:scale-95 ${
        active
          ? 'bg-[#E0F9FD] dark:bg-[#0D2A47] border-[#00B4DB] text-[#008CAE] dark:text-[#7EE8F5] shadow-[0_10px_28px_-10px_rgba(0,180,219,0.55)]'
          : 'bg-[#F0F6FA] dark:bg-[#091A30] border-[#D5E0EC] dark:border-[#1C395E] text-[#008CAE] dark:text-[#7EE8F5] group-hover:border-[#00B4DB]'
      }`}
    >
      {/* Subtle orbital ring */}
      <svg
        viewBox="0 0 48 48"
        aria-hidden="true"
        className="absolute inset-0 w-full h-full sb-orbital-spin opacity-60 group-hover:opacity-100 transition-opacity"
      >
        <circle
          cx="24"
          cy="24"
          r="21"
          fill="none"
          stroke="currentColor"
          strokeWidth="1"
          strokeDasharray="10 8"
        />
      </svg>

      {kind === 'WEB' && (
        <svg viewBox="0 0 24 24" className="w-5 h-5 relative z-10 transition-transform duration-300 group-hover:rotate-12" fill="none" stroke="currentColor" strokeWidth="1.8">
          <circle cx="12" cy="12" r="9" />
          <ellipse cx="12" cy="12" rx="4" ry="9" />
          <path d="M3.5 9h17M3.5 15h17" />
        </svg>
      )}
      {kind === 'MOBILE' && (
        <svg viewBox="0 0 24 24" className="w-5 h-5 relative z-10 transition-transform duration-300 group-hover:-translate-y-0.5" fill="none" stroke="currentColor" strokeWidth="1.8">
          <rect x="7" y="2.5" width="10" height="19" rx="2.5" />
          <path d="M10.5 18h3" strokeLinecap="round" />
          <path d="M10 8.5l2 2 3-3" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )}
      {kind === 'DESKTOP' && (
        <svg viewBox="0 0 24 24" className="w-5 h-5 relative z-10 transition-transform duration-300 group-hover:scale-105" fill="none" stroke="currentColor" strokeWidth="1.8">
          <rect x="3" y="4" width="18" height="12" rx="2" />
          <path d="M8 20h8M12 16v4" strokeLinecap="round" />
          <path d="M7.5 10l3 2.5L16.5 8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )}
    </div>
  );
};
