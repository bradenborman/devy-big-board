import { useState, useEffect } from 'react';

/**
 * Custom hook for detecting mobile screen sizes
 * Returns true if the viewport width is <= 768px
 * 
 * This hook automatically updates when the window is resized,
 * allowing components to dynamically switch between mobile and desktop views.
 * 
 * @param breakpoint - Optional custom breakpoint in pixels (default: 768)
 * @returns boolean indicating if the current viewport is mobile-sized
 * 
 * @example
 * ```tsx
 * const MyComponent = () => {
 *   const isMobile = useMobile();
 *   
 *   return isMobile ? <MobileView /> : <DesktopView />;
 * };
 * ```
 */
export const useMobile = (breakpoint: number = 768): boolean => {
    const [isMobile, setIsMobile] = useState<boolean>(
        typeof window !== 'undefined' ? window.innerWidth <= breakpoint : false
    );

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth <= breakpoint);
        };

        // Add event listener
        window.addEventListener('resize', handleResize);

        // Call handler right away so state gets updated with initial window size
        handleResize();

        // Cleanup
        return () => window.removeEventListener('resize', handleResize);
    }, [breakpoint]);

    return isMobile;
};
