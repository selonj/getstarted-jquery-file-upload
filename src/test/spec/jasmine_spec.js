describe('jasmine', function () {
  describe('assertions', function () {
    var foo = {}, bar = {};

    function error() {
      throw new Error('defect')
    }

    it('toBe', function () {
      expect('foo').toBe('foo');
      expect(foo).not.toBe(bar);
    });

    it('toEqual', function () {
      expect(foo).toEqual(bar);
    });

    it('toBeCloseTo', function () {
      expect(1.22).not.toBeCloseTo(1.23, 2);
      expect(1.22).toBeCloseTo(1.23, 1);
    });

    it('toBeDefined', function () {
      expect(true).toBeDefined();
      expect(false).toBeDefined();
      expect(undefined).not.toBeDefined();
    });

    it('toBeFalsy', function () {
      expect(false).toBeFalsy();
      expect(undefined).toBeFalsy();
      expect('').toBeFalsy();
      expect([]).not.toBeFalsy();
    });

    it('toBeGreaterThan', function () {
      expect(2).toBeGreaterThan(1);
      expect(1).not.toBeGreaterThan(1);
    });

    it('toBeLessThan', function () {
      expect(1).toBeLessThan(2);
      expect(2).not.toBeLessThan(1);
      expect(1).not.toBeLessThan(1);
    });

    it('toContain', function () {
      expect(['foo', 'bar']).toContain('foo');
      expect(['foo', 'bar']).not.toContain('others');
    });

    it('toHaveBeenCalled', function () {
      var foo = jasmine.createSpy('foo');

      expect(foo).not.toHaveBeenCalled();
      foo();
      expect(foo).toHaveBeenCalled();
    });

    it('toMatch', function () {
      expect('foo').toMatch(/f/);
      expect('foo').not.toMatch(/b/);
    });

    it('toThrow', function () {
      expect(error).toThrow(new Error('defect'));
    });

    it('toThrowError', function () {
      expect(error).toThrowError(Error);
      expect(error).toThrowError('defect');
      expect(error).toThrowError(/def/);
      expect(error).not.toThrowError(SyntaxError);
    });
  });

});