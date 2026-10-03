import { motion } from 'framer-motion';
import { ScanLine, Check, Camera, Upload, ShieldCheck } from 'lucide-react';

const Scanner = () => {
  const container = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.1 } }
  };
  const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } };

  return (
    <motion.div initial="hidden" animate="show" exit="hidden" variants={container}>
      <div className="page-header">
        <motion.h2 variants={item}>AI Medicine Scanner</motion.h2>
        <motion.p variants={item}>Scan prescriptions or medicine bottles to automatically add them to your schedule.</motion.p>
      </div>

      <div className="dashboard-grid" style={{ gridTemplateColumns: '2fr 1fr' }}>
        <motion.div variants={item} className="glass-card" style={{ padding: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '400px' }}>
          
          <div style={{ 
            width: '100%', 
            height: '300px', 
            border: '2px dashed rgba(255, 255, 255, 0.2)', 
            borderRadius: 'var(--radius-lg)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '1rem',
            background: 'rgba(0, 0, 0, 0.2)',
            marginBottom: '2rem'
          }}>
            <ScanLine size={48} className="text-primary" />
            <h3 style={{ color: 'var(--text-secondary)' }}>Point camera at medicine label</h3>
          </div>

          <div style={{ display: 'flex', gap: '1rem' }}>
            <button className="btn-primary" style={{ padding: '1rem 2rem' }}>
              <Camera size={20} />
              <span>Start Camera</span>
            </button>
            <button className="btn-secondary" style={{ padding: '1rem 2rem' }}>
              <Upload size={20} />
              <span>Upload Image</span>
            </button>
          </div>
        </motion.div>

        <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem' }}>
          <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldCheck className="text-success" />
            Verification Steps
          </h3>
          
          <div className="med-list">
            {[
              'Extracts Medication Name',
              'Identifies Dosage',
              'Checks Drug Interactions',
              'Sets up Schedule'
            ].map((step, idx) => (
              <div key={idx} className="med-item" style={{ padding: '1rem', background: 'rgba(0,0,0,0.2)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <div style={{ 
                    width: '24px', 
                    height: '24px', 
                    borderRadius: '50%', 
                    background: 'rgba(255,255,255,0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '0.75rem'
                  }}>
                    {idx + 1}
                  </div>
                  <span style={{ fontWeight: 500 }}>{step}</span>
                </div>
              </div>
            ))}
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
};

export default Scanner;
